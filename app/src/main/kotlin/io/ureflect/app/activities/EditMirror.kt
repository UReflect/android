package io.ureflect.app.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.services.isExpired
import io.ureflect.app.utils.*
import kotlinx.android.synthetic.main.activity_edit_mirror.*
import java.util.*


fun Context.editMirrorIntent(mirror: MirrorModel): Intent = Intent(this, EditMirror::class.java).apply { putExtra(EditMirror.MIRROR, mirror) }

class EditMirror : AppCompatActivity() {
    companion object {
        const val MIRROR = "Mirror"
        const val TAG = "NewMirrorActivity"
    }

    private var triedOnce = false
    private lateinit var queue: RequestQueue
    private lateinit var mirror: MirrorModel
    private lateinit var timezoneIds: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_mirror)
        Api.log("starting EditMirror activity")
        queue = Volley.newRequestQueue(this)

        getArg<MirrorModel>(MIRROR)?.let {
            mirror = it
        } ?: run {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        setupUI()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun payloadError(): Boolean {
        var error = false
        error = error || !evMirrorNameLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_name_required))
        error = error || !evMirrorLocationLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_location_required))
        return error
    }

    private fun payloadAutoValidate() {
        triedOnce = true
        evMirrorNameLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_name_required))
        evMirrorLocationLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_location_required))
    }

    private fun setupUI() {
        evMirrorName.setText(mirror.name)
        evMirrorLocation.setText(mirror.location)

        timezoneIds = TimeZone.getAvailableIDs().asList()
        spinnerTimezones.adapter = ArrayAdapter<String>(this, R.layout.view_timezone)
                .apply { setDropDownViewResource(R.layout.view_timezone_dropdown) }
                .apply { addAll(timezoneIds) }
        for (i in 0 until timezoneIds.size) {
            if (timezoneIds[i] == mirror.timezone) {
                spinnerTimezones.setSelection(i)
                break
            }
        }

        tvDelete.setOnClickListener {
            AlertDialog.Builder(this)
                    .apply { setTitle(R.string.confirmation_text) }
                    .apply { setMessage(R.string.mirror_confirmation_text) }
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
            if (!payloadError()) {
                update()
            } else if (!triedOnce) {
                payloadAutoValidate()
            }
        }
    }

    private fun delete() {
        loading.visibility = View.VISIBLE
        queue.add(Api.Mirror.unjoin(
                application,
                mirror.ID,
                Response.Listener {
                    loading.visibility = View.GONE
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.isExpired()) {
                        reLogin(loading, root, queue) {
                            delete()
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = NewMirror.TAG })
    }

    private fun update() {
        loading.visibility = View.VISIBLE
        queue.add(Api.Mirror.update(
                application,
                mirror.ID,
                JsonObject().apply { addProperty("name", evMirrorName.text.toString()) }
                        .apply { addProperty("location", evMirrorLocation.text.toString()) }
                        .apply { addProperty("timezone", timezoneIds[spinnerTimezones.selectedItemPosition]) },
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let {
                        mirror = it
                        successSnackbar(root)
                    } ?: run {
                        errorSnackbar(root, getString(R.string.api_parse_error))
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.isExpired()) {
                        reLogin(loading, root, queue) {
                            update()
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = NewMirror.TAG })
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply { putExtra(MirrorModel.TAG, mirror) })
        finish()
    }
}
