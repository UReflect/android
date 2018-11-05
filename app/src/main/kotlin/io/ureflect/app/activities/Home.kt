package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import io.ureflect.app.R
import io.ureflect.app.adapters.MirrorAdapter
import io.ureflect.app.mainIntent
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.models.User
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.utils.EqualSpacingItemDecoration
import io.ureflect.app.utils.Storage
import kotlinx.android.synthetic.main.activity_home.*
import java.text.SimpleDateFormat
import java.util.*


fun Context.homeIntent(): Intent {
    val intent = Intent(this, Home::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    return intent
}

class Home : AppCompatActivity() {
    private val TAG = "HomeActivity"
    private lateinit var queue: RequestQueue
    private lateinit var mirrors: ArrayList<MirrorModel>
    private lateinit var mirrorAdapter: MirrorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Api.log("starting home activity")
        queue = Volley.newRequestQueue(this)
        setupUI()
        loadMirrors()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        val formatter = SimpleDateFormat("EEEE dd MMMM", Locale.getDefault())
        tvDate.text = formatter.format(Date()).toUpperCase()

        val user = User.fromStorage(this.application)
        tvTitle.text = getString(R.string.home_title_text, user.name)

        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        rvMirrors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvMirrors.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.HORIZONTAL))

        btnLogout.transformationMethod = null
        btnRetry.setOnClickListener {
            loadMirrors()
        }

        btnLogout.transformationMethod = null
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadMirrors() {
        loading.visibility = View.VISIBLE
        btnRetry.visibility = View.GONE
        queue.add(Api.Mirror.all(
                this.application,
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let { mirrors ->
                        this.mirrors = mirrors
                        mirrorAdapter = MirrorAdapter(mirrors, {
                            startActivity(newMirrorIntent())
                        }, { mirror ->
                            startActivity(mirrorIntent(mirror))
                        })
                        rvMirrors.adapter = mirrorAdapter
                        return@Listener
                    }
                    btnRetry.visibility = View.VISIBLE
                    Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    btnRetry.visibility = View.VISIBLE
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ))
    }

    private fun logout() {
        Storage.clear(this.application)
        startActivity(mainIntent())
        finish()
    }
}
