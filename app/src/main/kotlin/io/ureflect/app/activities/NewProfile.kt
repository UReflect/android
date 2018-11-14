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
import io.ureflect.app.fragments.FacialRecognitionSetupFragment
import io.ureflect.app.fragments.NewProfileCompletedFragment
import io.ureflect.app.fragments.NewProfileFacialRecognitionMessageFragment
import io.ureflect.app.fragments.NewProfileNameFragment
import io.ureflect.app.models.ProfileModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import kotlinx.android.synthetic.main.activity_new_mirror.*
import java.util.*

fun Context.newProfileIntent(): Intent = Intent(this, NewProfile::class.java)

class NewProfile : AppCompatActivity() {
    companion object {
        val PROFILE = "profile"
    }

    private val TAG = "NewProfileActivity"
    private val NAME = 0
    private val FACIALMSG = 1
    private val COMPLETED = 2
    private val CREATE = 3
    private val FACIALSETUP = 4
    private lateinit var queue: RequestQueue
    private lateinit var adapter: ListFragmentPagerAdapter
    private var position = 0
    private val fragments = ArrayList<Fragment>()
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_profile)
        queue = Volley.newRequestQueue(this)
        setupFragments()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupFragments() {
        fragments.add(NewProfileNameFragment({
            next(FACIALMSG)
        }, { name: String ->
            this.name = name
        }))
        fragments.add(NewProfileFacialRecognitionMessageFragment({
            next(COMPLETED)
        }, {
            next(FACIALSETUP)
        }))
        fragments.add(NewProfileCompletedFragment {
            next(CREATE)
        })
        fragments.add(FacialRecognitionSetupFragment {
            next(COMPLETED)
        })
        adapter = ListFragmentPagerAdapter(supportFragmentManager, fragments)
        viewPager.adapter = adapter
        viewPager.currentItem = position
    }

    private fun next(i: Int) {
        position = i
        when (position) {
            CREATE -> createProfile()
            else -> Handler().postDelayed({ viewPager.currentItem = position }, 100)
        }
    }

    private fun createProfile() {
//        val data = JsonObject()
//        data.addProperty("title", name)
//        data.addProperty("timezone", TimeZone.getDefault().getDisplayName(Locale.US))
//
//        queue.add(Api.Profile.create(
//                application,
//                data,
//                Response.Listener { response ->
//                    response.data?.let {
//                        toProfileView(it)
//                    } ?: run {
//                        Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
//                    }
//                },
//                Response.ErrorListener { error ->
//                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
//                }
//        ))
    }

    private fun toProfileView(profile: ProfileModel) {
//        startActivity(profileIntent(mirror))
//        finish()
    }

    override fun onBackPressed() {
        when (position) {
            NAME -> super.onBackPressed()
            FACIALSETUP -> {
                position = FACIALMSG
                viewPager.currentItem = position
            }
            else -> {
                position -= 1
                viewPager.currentItem = position
            }
        }
    }
}
