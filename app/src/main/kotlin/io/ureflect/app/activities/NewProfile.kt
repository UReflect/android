package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.activities.Mirror.Companion.MIRROR
import io.ureflect.app.adapters.ListFragmentPagerAdapter
import io.ureflect.app.fragments.*
import io.ureflect.app.fragments.FacialRecognitionSetupFragment.Companion.NOT_HANDLED
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.models.ProfileModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.utils.getArg
import kotlinx.android.synthetic.main.activity_new_profile.*
import java.util.*

fun Context.newProfileIntent(mirror: MirrorModel): Intent = Intent(this, NewProfile::class.java).apply { putExtra(Mirror.MIRROR, mirror) }

class NewProfile : AppCompatActivity() {
    companion object {
        const val TAG = "NewProfileActivity"
    }

    private lateinit var profile: ProfileModel
    private lateinit var queue: RequestQueue
    private lateinit var mirror: MirrorModel
    private lateinit var adapter: ListFragmentPagerAdapter
    private var position = Steps.NAME.step
    private val fragments = ArrayList<CoordinatorRootFragment>()
    private lateinit var title: String
    private lateinit var pinCode: String
    private var skipFacial = true

    enum class Steps(val step: Int) {
        NAME(0),
        FACIAL_MSG(1),
        FACIAL_SETUP(2),
        PIN(3),
        COMPLETED(4)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_profile)
        queue = Volley.newRequestQueue(this)

        getArg<MirrorModel>(MIRROR)?.let {
            this.mirror = it
        } ?: finish()

        setupFragments()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupFragments() {
        fragments.add(
                NewProfileNameFragment { title ->
                    this.title = title
                    createProfile {
                        next(Steps.FACIAL_MSG)
                    }
                }
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
                }, { image ->
                    updateFacial(image)
                })
        )
        fragments.add(
                PinFragment { code ->
                    pinCode = code
                    updatePin {
                        next(Steps.COMPLETED)
                    }
                }.apply { isSetup = true })

        fragments.add(
                NewProfileCompletedFragment {
                    linkToMirror()
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

    private fun createProfile(then: () -> Unit) {
        val root = fragments[Steps.NAME.step].getRoot()
        val loader = fragments[Steps.NAME.step].getLoader()
        loader.visibility = View.VISIBLE
        queue.add(Api.Profile.create(
                application,
                JsonObject().apply { addProperty("title", title) },
                Response.Listener { response ->
                    loader.visibility = View.GONE
                    response.data?.let {
                        this.profile = it
                        then()
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

    private fun updateFacial(path: String) {
        val root = fragments[Steps.FACIAL_SETUP.step].getRoot()
        val loader = fragments[Steps.FACIAL_SETUP.step].getLoader()
        loader.visibility = View.VISIBLE
        queue.add(Api.Profile.setupFaces(
                application,
                profile.ID,
                Arrays.asList(path),
                Response.Listener {
                    loader.visibility = View.GONE
                    updatePin {
                        next(Steps.COMPLETED)
                    }
                },
                Response.ErrorListener { error ->
                    loader.visibility = View.GONE
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = TAG })
    }

    private fun updatePin(then: () -> Unit) {
        val root = fragments[Steps.PIN.step].getRoot()
        val loader = fragments[Steps.PIN.step].getLoader()
        loader.visibility = View.VISIBLE
        queue.add(Api.Profile.setupPin(
                application,
                profile.ID,
                JsonObject().apply { addProperty("pin", pinCode) },
                Response.Listener {
                    loader.visibility = View.GONE
                    then()
                },
                Response.ErrorListener { error ->
                    loader.visibility = View.GONE
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = TAG })
    }

    private fun linkToMirror() {
        val root = fragments[Steps.COMPLETED.step].getRoot()
        val loader = fragments[Steps.COMPLETED.step].getLoader()
        loader.visibility = View.VISIBLE
        queue.add(Api.Mirror.linkProfile(
                application,
                mirror.ID,
                JsonObject().apply { addProperty("profile_id", profile.ID) },
                Response.Listener {response ->
                    loader.visibility = View.INVISIBLE
                    response.data?.let {
                        finish()
                    } ?: run {
                        Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                    }
                },
                Response.ErrorListener { error ->
                    loader.visibility = View.INVISIBLE
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = TAG })
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