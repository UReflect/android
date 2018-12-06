package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.ureflect.app.R
import io.ureflect.app.models.ConnectedDeviceModel
import io.ureflect.app.services.Api
import io.ureflect.app.utils.*
import kotlinx.android.synthetic.main.activity_edit_connected_device.*

fun Context.editDeviceIntent(connectedDevice: ConnectedDeviceModel): Intent = Intent(this, EditConnectedDevice::class.java).apply { putExtra(ConnectedDeviceModel.TAG, connectedDevice) }

class EditConnectedDevice : AppCompatActivity() {
    companion object {
        const val TAG = "ConnectedDeviceActivity"
    }

    private var triedOnce = false
    private lateinit var queue: RequestQueue
    private lateinit var connectedDevice: ConnectedDeviceModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_connected_device)
        Api.log("starting EditConnectedDevice activity")
        queue = Volley.newRequestQueue(this)

        getArg<ConnectedDeviceModel>(ConnectedDeviceModel.TAG)?.let {
            connectedDevice = it
        } ?: finish()

        setupUI()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun titlePayloadError(): Boolean = !evDeviceTitleLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_name_required))

    private fun titlePayloadAutoValidate() {
        triedOnce = true
        evDeviceTitleLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_name_required))
    }

    private fun setupUI() {
        tvType.text = connectedDevice.description
        evDeviceTitle.setText(connectedDevice.name)

        tvDelete.setOnClickListener {
            AlertDialog.Builder(this)
                    .apply { setTitle(R.string.confirmation_text) }
                    .apply { setMessage(R.string.device_confirmation_text) }
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
        var devices = ArrayList<ConnectedDeviceModel>()
        fromStorage<ArrayList<ConnectedDeviceModel>>(application, ConnectedDeviceModel.LIST_TAG)?.let { them ->
            devices = them
        }
        devices.remove(connectedDevice)
        devices.toStorage(application, ConnectedDeviceModel.LIST_TAG)
        finish()
    }

    private fun updateTitle() {
        loading.visibility = View.VISIBLE
        var devices = ArrayList<ConnectedDeviceModel>()
        fromStorage<ArrayList<ConnectedDeviceModel>>(application, ConnectedDeviceModel.LIST_TAG)?.let { them ->
            devices = them
        }
        devices.find { device -> device.ID == connectedDevice.ID }?.name = evDeviceTitle.text.toString()
        devices.toStorage(application, ConnectedDeviceModel.LIST_TAG)
        loading.visibility = View.GONE
        successSnackbar(root)
        hideKeyboard()
    }
}
