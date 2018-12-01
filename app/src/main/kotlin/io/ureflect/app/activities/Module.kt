package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import io.ureflect.app.R
import io.ureflect.app.activities.Module.Companion.MODULE
import io.ureflect.app.adapters.CommentAdapter
import io.ureflect.app.adapters.EntityAdapter
import io.ureflect.app.models.CommentModel
import io.ureflect.app.models.ModuleModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.services.isExpired
import io.ureflect.app.utils.EqualSpacingItemDecoration
import io.ureflect.app.utils.errorSnackbar
import io.ureflect.app.utils.getArg
import io.ureflect.app.utils.reLogin
import kotlinx.android.synthetic.main.activity_module.*
import java.util.*

fun Context.moduleIntent(module: ModuleModel): Intent = Intent(this, Module::class.java).apply { putExtra(MODULE, module) }

class Module : AppCompatActivity() {
    companion object {
        const val MODULE = "module"
        const val TAG = "ModuleActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var module: ModuleModel
    private lateinit var comments: ArrayList<CommentModel>
    private lateinit var commentAdapter: EntityAdapter<CommentModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module)
        Api.log("starting module activity")
        queue = Volley.newRequestQueue(this)

        getArg<ModuleModel>(MODULE)?.let {
            module = it
        } ?: finish()

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        loadComments()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
        rvComments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvComments.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.VERTICAL, strict = true))

        tvTitle.text = module.title
        tvDescription.text = module.description
        tvPrice.text = if (module.price.toInt() == 0) getString(R.string.free_text) else getString(R.string.module_price_text, module.price.toString())

        btnInstall.setOnClickListener {
            install()
        }

        btnRetry.setOnClickListener {
            loadComments()
        }

        tvRating.text = getString(R.string.module_rating_text, module.rating_nb, if (module.rating_nb > 0) "s" else "")
        llRating.visibility = if (module.rating_nb != 0) View.VISIBLE else View.GONE
        var i = 1
        listOf(ivStar1, ivStar2, ivStar3, ivStar4, ivStar5).forEach { star ->
            star.setImageResource(if (module.rating >= i++) R.drawable.icon_star_active else R.drawable.icon_star_idle)
        }
    }

    private fun install() {

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
                            // TODO : Comment popup
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
