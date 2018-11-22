package io.ureflect.app.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.adapters.ListFragmentPagerAdapter
import io.ureflect.app.fragments.CoordinatorRootFragment
import io.ureflect.app.fragments.NewMirrorCodeFragment
import io.ureflect.app.fragments.NewMirrorLocationFragment
import io.ureflect.app.fragments.NewMirrorNameFragment
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import kotlinx.android.synthetic.main.activity_new_mirror.*
import kotlinx.android.synthetic.main.fragment_new_mirror_code.*
import java.util.*

fun Context.newMirrorIntent(): Intent = Intent(this, NewMirror::class.java)

class NewMirror : AppCompatActivity() {
    companion object {
        const val TAG = "NewMirrorActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var adapter: ListFragmentPagerAdapter
    private var position = Steps.CODE.step
    private val fragments = ArrayList<CoordinatorRootFragment>()
    private lateinit var name: String
    private lateinit var location: String
    private lateinit var mirrorId: String

    enum class Steps(val step: Int) {
        CODE(0),
        NAME(1),
        LOCATION(2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_mirror)
        queue = Volley.newRequestQueue(this)
        setupFragments()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupFragments() {
        fragments.add(
                NewMirrorCodeFragment {
                    join {
                        next(Steps.NAME)
                    }
                }
        )
        fragments.add(
                NewMirrorNameFragment { name ->
                    this.name = name
                    next(Steps.LOCATION)
                })
        fragments.add(
                NewMirrorLocationFragment { location ->
                    this.location = location
                    createMirror()
                }
        )
        adapter = ListFragmentPagerAdapter(supportFragmentManager, fragments)
        viewPager.adapter = adapter
        viewPager.currentItem = position
    }

    private fun next(step: Steps) {
        position = step.step
        Handler().postDelayed({ viewPager.currentItem = position }, 100)
    }

    private fun join(then: () -> Unit) {
        val root = fragments[Steps.CODE.step].getRoot()
        val loader = fragments[Steps.CODE.step].getLoader()
        loader.visibility = View.VISIBLE
        queue.add(Api.Mirror.join(
                application,
                JsonObject().apply { addProperty("join_code", civCode.code) },
                Response.Listener { response ->
                    loader.visibility = View.GONE
                    response.data?.ID?.let { mirrorId ->
                        this.mirrorId = mirrorId
                        then()
                    } ?: run {
                        Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                        hideKeyboard()
                    }
                },
                Response.ErrorListener { error ->
                    loader.visibility = View.GONE
                    hideKeyboard()
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = TAG })
    }

    private fun hideKeyboard() {
        var view = currentFocus
        if (view == null) {
            view = View(this)
        }
        getSystemService(Activity.INPUT_METHOD_SERVICE).let {
            (it as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun createMirror() {
        val root = fragments[Steps.LOCATION.step].getRoot()
        val loader = fragments[Steps.LOCATION.step].getLoader()
        loader.visibility = View.VISIBLE
        queue.add(Api.Mirror.update(
                application,
                mirrorId,
                JsonObject().apply { addProperty("name", name) }
                        .apply { addProperty("location", location) }
                        .apply { addProperty("timezone", TimeZone.getDefault().id) },
                Response.Listener { response ->
                    loader.visibility = View.GONE
                    response.data?.let {
                        finish()
                    } ?: run {
                        Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                    }
                },
                Response.ErrorListener { error ->
                    loader.visibility = View.GONE
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = TAG })
    }

    override fun onBackPressed() {
        when (position) {
            Steps.CODE.step -> super.onBackPressed()
            Steps.NAME.step -> Unit
            else -> {
                position -= 1
                viewPager.currentItem = position
            }
        }
    }
}
