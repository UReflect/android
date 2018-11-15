package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.ureflect.app.R
import io.ureflect.app.adapters.ListFragmentPagerAdapter
import io.ureflect.app.fragments.*
import io.ureflect.app.models.ProfileModel
import kotlinx.android.synthetic.main.activity_new_mirror.*
import java.util.*

fun Context.newProfileIntent(): Intent = Intent(this, NewProfile::class.java)

class NewProfile : AppCompatActivity() {
    private val TAG = "NewProfileActivity"
    private val NAME = 0
    private val FACIALMSG = 1
    private val FACIALSETUP = 2
    private val PIN = 3
    private val COMPLETED = 4
    private val CREATE = 5
    private lateinit var queue: RequestQueue
    private lateinit var adapter: ListFragmentPagerAdapter
    private var position = 0
    private val fragments = ArrayList<Fragment>()
    private lateinit var name: String
    private lateinit var code: String
    private var images: List<String> = ArrayList()
    private var skipFacial = true

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
            next(PIN)
            skipFacial = true
        }, {
            next(FACIALSETUP)
            skipFacial = false
        }))
        fragments.add(FacialRecognitionSetupFragment({
            next(PIN)
        }, {
            images = it
        }))
        fragments.add(PinFragment({
            next(COMPLETED)
        }, {
            code = it
        }).apply { isSetup = true })
        fragments.add(NewProfileCompletedFragment {
            next(CREATE)
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
            PIN -> {
                position = when(skipFacial) {
                    true -> FACIALMSG
                    else -> FACIALSETUP
                }
                viewPager.currentItem = position
            }
            FACIALSETUP -> {
                if (!(fragments[FACIALSETUP] as FacialRecognitionSetupFragment).backPressed()) {
                    position = FACIALMSG
                    viewPager.currentItem = position
                }
            }
            else -> {
                position -= 1
                viewPager.currentItem = position
            }
        }
    }
}
