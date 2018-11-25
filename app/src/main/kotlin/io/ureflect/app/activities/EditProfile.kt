package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.fragments.BackPressedFragment.Companion.NOT_HANDLED
import io.ureflect.app.fragments.FacialRecognitionSetupFragment
import io.ureflect.app.fragments.PinFragment
import io.ureflect.app.models.ProfileModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.services.expired
import io.ureflect.app.utils.*
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.util.*


fun Context.editProfileIntent(profile: ProfileModel): Intent = Intent(this, EditProfile::class.java).apply { putExtra(EditProfile.PROFILE, profile) }

class EditProfile : AppCompatActivity() {
    companion object {
        const val PROFILE = "Profile"
        const val TAG = "ProfileActivity"
    }

    private var triedOnce = false
    private lateinit var queue: RequestQueue
    private lateinit var profile: ProfileModel
    private lateinit var verifyPinFragment: PinFragment
    private lateinit var setPinFragment: PinFragment
    private lateinit var facialFragment: FacialRecognitionSetupFragment
    private var step = Steps.OUT

    enum class Steps(val step: Int) {
        OUT(0),
        VERIFY_PIN(1),
        SET_PIN(1),
        FACIAL(2),
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Api.log("starting EditProfile activity")
        queue = Volley.newRequestQueue(this)

        getArg<ProfileModel>(PROFILE)?.let {
            profile = it
        } ?: finish()

        setupUI()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun titlePayloadError(): Boolean = !evProfileTitleLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_name_required))

    private fun titlePayloadAutoValidate() {
        triedOnce = true
        evProfileTitleLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_name_required))
    }

    private fun setupUI() {
        evProfileTitle.setText(profile.title)

        tvPin.setOnClickListener {
            verifyPinFragment = PinFragment { pin ->
                verifyPin(pin) {
                    setPinFragment = PinFragment { pin ->
                        updatePin(pin) {
                            removeFragment(setPinFragment)
                            step = Steps.OUT
                            successSnackbar(root)
                        }
                    }
                    step = Steps.SET_PIN
                    replaceFragment(setPinFragment, R.id.flFragment)
                }
            }.apply { isDoublePass = false }
            hideKeyboard()
            addFragment(verifyPinFragment, R.id.flFragment)
            step = Steps.VERIFY_PIN
        }

        tvFacial.setOnClickListener {
            facialFragment = FacialRecognitionSetupFragment({
                removeFragment(facialFragment)
                step = Steps.OUT
                successSnackbar(root)
            }, { path: String, callback: () -> Unit ->
                updateFacial(path) {
                    callback()
                }
            })
            hideKeyboard()
            addFragment(facialFragment, R.id.flFragment)
            step = Steps.FACIAL
        }

        tvDelete.setOnClickListener {
            AlertDialog.Builder(this)
                    .apply { setTitle(R.string.confirmation_text) }
                    .apply { setMessage(R.string.profile_confirmation_text) }
                    .apply {
                        setPositiveButton("Yes") { dialog, _ ->
                            dialog.dismiss()
                            delete()
                        }
                    }
                    .apply {
                        setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                    }
                    .create()
                    .show()
        }

        btn.setOnClickListener {
            if (!titlePayloadError()) {
                updateTitle()
            } else if (!triedOnce) {
                titlePayloadAutoValidate()
            }
        }
    }

    private fun delete() {
        loading.visibility = View.VISIBLE
        queue.add(Api.Profile.delete(
                application,
                profile.ID,
                Response.Listener {
                    loading.visibility = View.GONE
                    finish()
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)), error.expired())
                }
        ).apply { tag = NewProfile.TAG })
    }

    private fun updateFacial(path: String, callback: () -> Unit) {
        val root = facialFragment.getRoot()
        val loader = facialFragment.getLoader()
        loader.visibility = View.VISIBLE
        queue.add(Api.Profile.setupFaces(
                application,
                profile.ID,
                Arrays.asList(path),
                Response.Listener {
                    loader.visibility = View.GONE
                    successSnackbar(root)
                    callback()
                },
                Response.ErrorListener { error ->
                    loader.visibility = View.GONE
                    errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)), error.expired())
                }
        ).apply { tag = NewProfile.TAG })
    }

    private fun verifyPin(pin: String, callback: () -> Unit) {
        val root = verifyPinFragment.getRoot()
        val loader = verifyPinFragment.getLoader()
        loader.visibility = View.VISIBLE
        queue.add(Api.Profile.verifyPin(
                application,
                profile.ID,
                JsonObject().apply { addProperty("pin", pin) },
                Response.Listener {
                    loader.visibility = View.INVISIBLE
                    callback()
                },
                Response.ErrorListener { error ->
                    loader.visibility = View.INVISIBLE
                    errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)), error.expired())
                }
        ).apply { tag = NewProfile.TAG })

    }

    private fun updatePin(pin: String, callback: () -> Unit) {
        val root = setPinFragment.getRoot()
        val loader = setPinFragment.getLoader()
        loader.visibility = View.VISIBLE
        queue.add(Api.Profile.setupPin(
                application,
                profile.ID,
                JsonObject().apply { addProperty("pin", pin) },
                Response.Listener {
                    loader.visibility = View.INVISIBLE
                    callback()
                },
                Response.ErrorListener { error ->
                    loader.visibility = View.INVISIBLE
                    errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)), error.expired())
                }
        ).apply { tag = NewProfile.TAG })

    }

    private fun updateTitle() {
        loading.visibility = View.VISIBLE
        queue.add(Api.Profile.update(
                application,
                profile.ID,
                JsonObject().apply { addProperty("title", evProfileTitle.text.toString()) },
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let {
                        profile = it
                        successSnackbar(root)
                    } ?: run {
                        errorSnackbar(root, getString(R.string.api_parse_error))
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)), error.expired())
                }
        ).apply { tag = NewProfile.TAG })
    }

    override fun onBackPressed() {
        when (step) {
            Steps.VERIFY_PIN -> {
                if (verifyPinFragment.backPressed() == NOT_HANDLED) {
                    removeFragment(verifyPinFragment)
                    step = Steps.OUT
                }
            }
            Steps.SET_PIN -> {
                if (setPinFragment.backPressed() == NOT_HANDLED) {
                    removeFragment(setPinFragment)
                    step = Steps.OUT
                }
            }
            Steps.FACIAL -> {
                if (facialFragment.backPressed() == NOT_HANDLED) {
                    removeFragment(facialFragment)
                    step = Steps.OUT
                }
            }
            else -> super.onBackPressed()
        }
    }
}
