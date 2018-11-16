package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.adapters.ListFragmentPagerAdapter
import io.ureflect.app.fragments.*
import io.ureflect.app.fragments.FacialRecognitionSetupFragment.Companion.NOT_HANDLED
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.models.ProfileModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import kotlinx.android.synthetic.main.activity_new_profile.*
import java.util.*

fun Context.newProfileIntent(mirror: MirrorModel): Intent = Intent(this, NewProfile::class.java).apply { putExtra(Mirror.MIRROR, mirror) }

class NewProfile : AppCompatActivity() {
    companion object {
        const val TAG = "NewProfileActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var mirror: MirrorModel
    private lateinit var adapter: ListFragmentPagerAdapter
    private var position = Steps.NAME.step
    private val fragments = ArrayList<Fragment>()
    private lateinit var title: String
    private lateinit var pinCode: String
    private var images: List<String> = ArrayList()
    private var skipFacial = true

    enum class Steps(val step: Int) {
        NAME(0),
        FACIAL_MSG(1),
        FACIAL_SETUP(2),
        PIN(3),
        COMPLETED(4),
        CREATE(5)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_profile)
        queue = Volley.newRequestQueue(this)

        val args = intent.extras
        args?.getSerializable(Mirror.MIRROR)?.let { mirror ->
            this.mirror = mirror as MirrorModel
        } ?: run {
            finish()
        }

        setupFragments()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupFragments() {
        fragments.add(
                NewProfileNameFragment({
                    next(Steps.FACIAL_MSG)
                }, { name: String ->
                    this.title = name
                })
        )
        fragments.add(
                NewProfileFacialRecognitionMessageFragment({
                    next(Steps.PIN)
                    skipFacial = true
                }, {
                    next(Steps.FACIAL_SETUP)
                    skipFacial = false
                })
        )
        fragments.add(
                FacialRecognitionSetupFragment({
                    next(Steps.PIN)
                }, {
                    images = it
                })
        )
        fragments.add(
                PinFragment({
                    next(Steps.COMPLETED)
                }, {
                    pinCode = it
                }
                ).apply { isSetup = true })

        fragments.add(
                NewProfileCompletedFragment {
                    next(Steps.CREATE)
                }
        )
        adapter = ListFragmentPagerAdapter(supportFragmentManager, fragments)
        viewPager.adapter = adapter
        viewPager.currentItem = position
    }

    private fun next(step: Steps) {
        position = step.step
        when (position) {
            Steps.CREATE.step -> createProfile()
            else -> Handler().postDelayed({ viewPager.currentItem = position }, 100)
        }
    }

    private fun createProfile() {
        loading.visibility = View.VISIBLE
        queue.add(Api.Profile.create(
                application,
                JsonObject().apply { addProperty("title", title) },
                Response.Listener { response ->
                    response.data?.let {
                        updateFacial(it)
                    } ?: run {
                        loading.visibility = View.GONE
                        Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = TAG })
    }

    private fun updateFacial(profile: ProfileModel) {
        if (images.isNotEmpty()) {
            queue.add(Api.Profile.setupFaces(
                    application,
                    profile.ID,
                    images,
                    Response.Listener {
                        updatePin(profile)
                    },
                    Response.ErrorListener { error ->
                        loading.visibility = View.GONE
                        Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                    }
            ).apply { tag = TAG })
        } else {
            updatePin(profile)
        }
    }

    private fun updatePin(profile: ProfileModel) {
        queue.add(Api.Profile.setupPin(
                application,
                profile.ID,
                JsonObject().apply { addProperty("pin", pinCode) },
                Response.Listener {
                    linkToMirror(profile)
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = TAG })
    }

    private fun linkToMirror(profile: ProfileModel) {
        queue.add(Api.Mirror.linkProfile(
                application,
                mirror.ID,
                JsonObject().apply { addProperty("profile_id", profile.ID) },
                Response.Listener {
                    toProfileView(profile)
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = TAG })
    }

    private fun toProfileView(profile: ProfileModel) {
        startActivity(profileIntent(profile))
        finish()
    }

    override fun onBackPressed() {
        when (position) {
            Steps.NAME.step -> super.onBackPressed()
            Steps.PIN.step -> {
                position = when (skipFacial) {
                    true -> Steps.FACIAL_MSG.step
                    else -> Steps.FACIAL_SETUP.step
                }
                viewPager.currentItem = position
            }
            Steps.FACIAL_SETUP.step -> {
                if ((fragments[Steps.FACIAL_SETUP.step] as FacialRecognitionSetupFragment).backPressed() == NOT_HANDLED) {
                    position = Steps.FACIAL_MSG.step
                    viewPager.currentItem = position
                }
            }
            Steps.COMPLETED.step -> Unit
            else -> {
                position -= 1
                viewPager.currentItem = position
            }
        }
    }
}
