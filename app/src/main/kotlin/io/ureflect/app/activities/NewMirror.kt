package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.adapters.ListFragmentPagerAdapter
import io.ureflect.app.fragments.NewMirrorCodeFragment
import io.ureflect.app.fragments.NewMirrorLocationFragment
import io.ureflect.app.fragments.NewMirrorNameFragment
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import kotlinx.android.synthetic.main.activity_new_mirror.*

fun Context.newMirrorIntent(): Intent {
    return Intent(this, NewMirror::class.java)
}

class NewMirror : AppCompatActivity() {
    private val TAG = "NewMirrorActivity"
    private lateinit var queue: RequestQueue
    private lateinit var adapter: ListFragmentPagerAdapter
    private var position = 0
    private val fragments = ArrayList<Fragment>()
    private lateinit var name: String
    private lateinit var location: String
    private lateinit var mirrorId: String

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
        fragments.add(NewMirrorCodeFragment({ i: Int ->
            next(i)
        }, { mirrorId: String ->
            this.mirrorId = mirrorId
        }))
        fragments.add(NewMirrorNameFragment({ i: Int ->
            next(i)
        }, { name: String ->
            this.name = name
        }))
        fragments.add(NewMirrorLocationFragment({ i: Int ->
            next(i)
        }, { location: String ->
            this.location = location
        }))
        adapter = ListFragmentPagerAdapter(supportFragmentManager, fragments)
        viewPager.adapter = adapter
        viewPager.currentItem = position
    }

    private fun next(i: Int) {
        position = i + 1
        if (position < fragments.size) {
            Handler().postDelayed({ viewPager.currentItem = i + 1 }, 100)
        } else if (position == fragments.size) {
            createMirror()
        }
    }

    private fun createMirror() {
        val data = JsonObject()
        data.addProperty("name", name)
        data.addProperty("location", location)

        queue.add(Api.Mirror.update(
                application,
                mirrorId,
                data,
                Response.Listener { response ->
                    response.data?.let {
                        toMirrorView(it)
                    } ?: run {
                        Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                    }
                },
                Response.ErrorListener { error ->
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ))
    }

    private fun toMirrorView(mirror: MirrorModel) {
        startActivity(mirrorIntent(mirror))
        finish()
    }

    override fun onBackPressed() {
        if (position != 1) {
            if (position == 0) {
                super.onBackPressed()
            } else {
                position -= 1
                viewPager.currentItem = position
            }
        }
    }
}
