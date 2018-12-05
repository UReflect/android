package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.adapters.CommentAdapter
import io.ureflect.app.adapters.EntityAdapter
import io.ureflect.app.models.CommentModel
import io.ureflect.app.models.ModuleModel
import io.ureflect.app.models.ProfileModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.services.isExpired
import io.ureflect.app.utils.*
import kotlinx.android.synthetic.main.activity_module.*
import java.util.*

fun Context.moduleIntent(module: ModuleModel, profileId: Long): Intent = Intent(this, Module::class.java)
        .apply { putExtra(ModuleModel.TAG, module) }
        .apply { putExtra(ProfileModel.TAG, profileId) }

class Module : AppCompatActivity() {
    companion object {
        const val RATING = "rating"
        const val TAG = "ModuleActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var module: ModuleModel
    private var profileId: Long = -1
    private lateinit var comments: ArrayList<CommentModel>
    private lateinit var commentAdapter: EntityAdapter<CommentModel>
    private var rating = -1
    private lateinit var installedModules: ArrayList<ModuleModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module)
        Api.log("starting module activity")
        queue = Volley.newRequestQueue(this)

        getArg<ModuleModel>(ModuleModel.TAG)?.let {
            module = it
        } ?: finish()

        getArg<Long>(ProfileModel.TAG)?.let {
            profileId = it
        } ?: finish()

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        loadComments()

        fromStorage<ArrayList<ModuleModel>>(application, ModuleModel.LIST_TAG)?.let {
            installedModules = it
        } ?: finish()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
        rvComments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvComments.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.VERTICAL, strict = true))

        btnInstall.text = if (module.is_installed) getString(R.string.uninstall_btn_text) else getString(R.string.install_btn_text)
        btnInstall.setOnClickListener {
            installOrUninstallModule(module)
        }

        btnRetry.setOnClickListener {
            loadComments()
        }

        tvTitle.text = module.title
        tvDescription.text = module.description
        tvPrice.text = if (module.price.toInt() == 0) getString(R.string.free_text) else getString(R.string.module_price_text, module.price.toString())

        setupRatings()
    }

    private fun setupRatings() {
        tvRating.text = resources.getQuantityString(R.plurals.module_ratings_text, module.rating_nb, module.rating_nb)

        llRating.visibility = if (module.rating_nb != 0) View.VISIBLE else View.GONE
        listOf(ivStar1, ivStar2, ivStar3, ivStar4, ivStar5).forEachIndexed { i: Int, star ->
            star.setImageResource(if (module.rating >= i + 1) R.drawable.icon_star_active else R.drawable.icon_star_idle)
        }

        val rate = module.your_rating?.value ?: 0
        listOf(ivStar21, ivStar22, ivStar23, ivStar24, ivStar25).forEachIndexed { i: Int, star ->
            star.setImageResource(if (rate >= i + 1) R.drawable.icon_star_active else R.drawable.icon_star_idle)
            star.setOnClickListener {
                registerRate(i + 1)
            }
        }

        if (module.your_rating != null) {
            tvRateMsg.text = getString(R.string.module_rate_update_text)
            btnRate.text = getString(R.string.update_btn_text)
        } else {
            tvRateMsg.text = getString(R.string.module_rate_text)
            btnRate.text = getString(R.string.rate_btn_text)
        }

        btnRate.transformationMethod = null
        btnRate.setOnClickListener {
            rate()
        }
    }

    private fun registerRate(rate: Int) {
        val myRate = module.your_rating?.value ?: 0
        btnRate.visibility = if (rate != myRate) View.VISIBLE else View.GONE
        listOf(ivStar21, ivStar22, ivStar23, ivStar24, ivStar25).forEachIndexed { i: Int, star ->
            star.setImageResource(if (rate >= i + 1) R.drawable.icon_star_active else R.drawable.icon_star_idle)
        }
        this.rating = rate
    }

    private fun showCommentPopup() {
        val input: EditText = layoutInflater.inflate(R.layout.view_new_comment, null) as EditText
        AlertDialog.Builder(this)
                .apply { title = getString(R.string.module_comment_text) }
                .apply { setMessage(R.string.enter_comment_text) }
                .apply { setView(input) }
                .apply {
                    setPositiveButton(R.string.module_comment_text) { _, _ ->
                        val value = input.text.trim()
                        if (!value.isEmpty()) {
                            comment(value.toString())
                        }
                    }
                }
                .apply { setNegativeButton(R.string.cancel_btn_text) { _, _ -> } }
                .apply { show() }
    }

    private fun comment(comment: String) {
        loading.visibility = View.VISIBLE
        queue.add(Api.Module.comment(
                application,
                module.ID,
                JsonObject().apply { addProperty("value", comment) },
                Response.Listener {
                    loading.visibility = View.GONE
                    loadComments()
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.isExpired()) {
                        reLogin(loading, root, queue) {
                            comment(comment)
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = TAG })
    }

    private fun installOrUninstallModule(module: ModuleModel) {
        if (module.is_installed) {
            uninstallModule(module)
        } else {
            installModule(module)
        }
    }

    private fun uninstallModule(module: ModuleModel) {
        loading.visibility = View.VISIBLE
        queue.add(Api.Module.uninstall(
                application,
                module.ID,
                profileId,
                Unit,
                Response.Listener {
                    loading.visibility = View.GONE
                    successSnackbar(root)
                    module.is_installed = false
                    installedModules.remove(module)
                    installedModules.toStorage(application, ModuleModel.LIST_TAG)
                    btnInstall.text = getString(R.string.install_btn_text)
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.isExpired()) {
                        reLogin(loading, root, queue) {
                            installModule(module)
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = Module.TAG })
    }

    private fun installModule(module: ModuleModel) {
        loading.visibility = View.VISIBLE
        queue.add(Api.Module.install(
                application,
                module.ID,
                profileId,
                Unit,
                Response.Listener {
                    loading.visibility = View.GONE
                    successSnackbar(root)
                    module.is_installed = true
                    installedModules.add(module)
                    installedModules.toStorage(application, ModuleModel.LIST_TAG)
                    btnInstall.text = getString(R.string.uninstall_btn_text)
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.isExpired()) {
                        reLogin(loading, root, queue) {
                            installModule(module)
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = Module.TAG })
    }

    private fun rate() {
        loading.visibility = View.VISIBLE
        queue.add(Api.Module.rate(
                application,
                module.ID,
                JsonObject().apply { addProperty("value", rating) },
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let { module ->
                        this.module = module
                        setupRatings()
                        registerRate(rating)
                    } ?: run {
                        errorSnackbar(root, getString(R.string.api_parse_error))
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.isExpired()) {
                        reLogin(loading, root, queue) {
                            rate()
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = TAG })
    }

    private fun loadComments() {
        loading.visibility = View.VISIBLE
        btnRetry.visibility = View.GONE
        queue.add(Api.Module.comments(
                application,
                module.ID,
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let { comments ->
                        this.comments = comments
                        commentAdapter = CommentAdapter(comments) { _: CommentModel?, _: View ->
                            showCommentPopup()
                        }
                        rvComments.adapter = commentAdapter
                    } ?: run {
                        btnRetry.visibility = View.VISIBLE
                        errorSnackbar(root, getString(R.string.api_parse_error))
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    btnRetry.visibility = View.VISIBLE
                    if (error.isExpired()) {
                        reLogin(loading, root, queue) {
                            loadComments()
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = TAG })
    }
}
