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
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.services.isExpired
import io.ureflect.app.utils.EqualSpacingItemDecoration
import io.ureflect.app.utils.errorSnackbar
import io.ureflect.app.utils.reLogin
import kotlinx.android.synthetic.main.activity_store.*
import java.util.*

fun Context.storeIntent(): Intent {
    val intent = Intent(this, Store::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    return intent
}

class Store : AppCompatActivity() {
    companion object {
        const val TAG = "StoreActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var modules: ArrayList<ModuleModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        Api.log("starting store activity")
        queue = Volley.newRequestQueue(this)
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        loadModules()
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
        //invert
        //limit
        return "$query$order"
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
                        this.modules = modules
                        rvModules.adapter = ModuleAdapter(modules, { module: ModuleModel?, view: View ->
                            module?.let { startActivity(moduleIntent(module)) }
                        }, { module: ModuleModel?, view: View ->
                            //TODO : install
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
