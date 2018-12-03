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
import io.ureflect.app.adapters.ModuleAdapter
import io.ureflect.app.models.ModuleModel
import io.ureflect.app.models.ProfileModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.services.isExpired
import io.ureflect.app.utils.*
import kotlinx.android.synthetic.main.activity_store.*

fun Context.storeIntent(profileId: Long): Intent = Intent(this, Store::class.java).apply { putExtra(ProfileModel.TAG, profileId) }

class Store : AppCompatActivity() {
    companion object {
        const val TAG = "StoreActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var modules: ArrayList<ModuleModel>
    private var profileId: Long = -1
    private lateinit var installedModules: ArrayList<ModuleModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        Api.log("starting store activity")

        getArg<Long>(ProfileModel.TAG)?.let {
            profileId = it
        } ?: finish()

        queue = Volley.newRequestQueue(this)
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        loadModules()

        fromStorage<ArrayList<ModuleModel>>(application, ModuleModel.LIST_TAG)?.let {
            installedModules = it
        } ?: finish()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        rvModules.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvModules.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.VERTICAL))

        svSearch.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true.apply { loadModules() }
            override fun onQueryTextChange(newText: String?): Boolean = true.apply { loadModules() }
        })

        btnRetry.transformationMethod = null
        btnRetry.setOnClickListener {
            loadModules()
        }
    }

    private fun emptySearch(): Boolean = svSearch.query.isEmpty()

    private fun buildQuery(): String {
        val query = if (!emptySearch()) "query=" + svSearch.query + '&' else ""
        val order = "order=title"
        return "$query$order"
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
                    rvModules.adapter.notifyDataSetChanged()
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
                    rvModules.adapter.notifyDataSetChanged()
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

    private fun loadModules() {
        loading.visibility = View.VISIBLE
        btnRetry.visibility = View.GONE
        queue.add(Api.Module.all(
                application,
                buildQuery(),
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let { modules ->
                        modules.forEach {
                            it.is_installed = installedModules.contains(it)
                        }
                        this.modules = modules
                        rvModules.adapter = ModuleAdapter(modules, { module: ModuleModel?, _: View ->
                            module?.let { startActivity(moduleIntent(module, profileId)) }
                        }, { module: ModuleModel?, view: View ->
                            module?.let { installOrUninstallModule(module) }
                        })
                        tvEmpty.visibility = if (modules.isEmpty()) View.VISIBLE else View.GONE
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
                            loadModules()
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = TAG })
    }
}
