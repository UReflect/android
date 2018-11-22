package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.models.ProfileModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.utils.autoValidate
import io.ureflect.app.utils.getArg
import io.ureflect.app.utils.validate
import kotlinx.android.synthetic.main.activity_profile.*


fun Context.profileIntent(profile: ProfileModel): Intent = Intent(this, Profile::class.java).apply { putExtra(Profile.PROFILE, profile) }

class Profile : AppCompatActivity() {
    companion object {
        const val PROFILE = "Profile"
        const val TAG = "ProfileActivity"
    }

    private var triedOnce = false
    private lateinit var queue: RequestQueue
    private lateinit var profile: ProfileModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Api.log("starting Profile activity")
        queue = Volley.newRequestQueue(this)

        getArg<ProfileModel>(PROFILE)?.let {
            this.profile = it
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

        //TODO : update pin and facial

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
                update()
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
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = NewProfile.TAG })
    }

    private fun update() {
        loading.visibility = View.VISIBLE
        queue.add(Api.Profile.update(
                application,
                profile.ID,
                JsonObject().apply { addProperty("title", evProfileTitle.text.toString()) },
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let {
                        this.profile = it
                    } ?: run {
                        Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = NewProfile.TAG })
    }
}
