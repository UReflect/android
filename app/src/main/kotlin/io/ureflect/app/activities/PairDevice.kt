package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.ureflect.app.R
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.utils.autoValidate
import io.ureflect.app.utils.getArg
import io.ureflect.app.utils.validate
import kotlinx.android.synthetic.main.activity_pair_device.*

fun Context.pairDeviceIntent(mirror: MirrorModel): Intent = Intent(this, PairDevice::class.java).apply { putExtra(MirrorModel.TAG, mirror) }

class PairDevice : AppCompatActivity() {
    companion object {
        const val TAG = "PairDeviceActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var mirror: MirrorModel
    private var triedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_device)
        queue = Volley.newRequestQueue(this)

        getArg<MirrorModel>(MirrorModel.TAG)?.let {
            mirror = it
        } ?: finish()

        setupUI()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun payloadError(): Boolean = !evNameLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_name_required))

    private fun payloadAutoValidate() = evNameLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_name_required)).apply { triedOnce = true }

    private fun setupUI() {
        btn.setOnClickListener {
            if (!payloadError()) {
                createDevice()
            } else if (!triedOnce) {
                payloadAutoValidate()
            }
        }
    }

    private fun createDevice() {
//        loading.visibility = View.VISIBLE
//        queue.add(Api.Device.create(
//                application,
//                JsonObject().apply { addProperty("name", evName.text) },
//                Response.Listener { response ->
//                    loading.visibility = View.INVISIBLE
//                    response.data?.let {
//                        finish()
//                    } ?: run {
//                        errorSnackbar(root, getString(R.string.api_parse_error))
//                    }
//                },
//                Response.ErrorListener { error ->
//                    loading.visibility = View.INVISIBLE
//                    if (error.isExpired()) {
//                        reLogin(loading, root, queue) {
//                            createDevice(callback)
//                        }
//                    } else {
//                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
//                    }
//                }
//        ).apply { tag = TAG })
    }
}
