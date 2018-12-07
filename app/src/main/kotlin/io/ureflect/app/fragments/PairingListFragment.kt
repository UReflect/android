package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import io.ureflect.app.R
import io.ureflect.app.activities.Mirror
import io.ureflect.app.adapters.DeviceAdapter
import io.ureflect.app.adapters.EntityAdapter
import io.ureflect.app.models.ConnectedDeviceModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.utils.EqualSpacingItemDecoration
import io.ureflect.app.utils.errorSnackbar
import kotlinx.android.synthetic.main.fragment_pairing_list.*

@SuppressLint("ValidFragment")
class PairingListFragment(var next: (ConnectedDeviceModel) -> Unit) : CoordinatorRootFragment() {
    private lateinit var devices: ArrayList<ConnectedDeviceModel>
    private lateinit var deviceAdapter: EntityAdapter<ConnectedDeviceModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_pairing_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvDevices.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).toInt()
        rvDevices.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.VERTICAL))
        loadDevices()
    }

    private fun loadDevices() {
        loading.visibility = View.VISIBLE
        activity?.application?.let {
            Volley.newRequestQueue(activity).add(Api.Device.all(
                    it,
                    it.assets.open("connectedDevices.json"),
                    Response.Listener { response ->
                        loading.visibility = View.GONE
                        response.data?.let { devices ->
                            this.devices = devices
                            deviceAdapter = DeviceAdapter(devices) { device: ConnectedDeviceModel?, _: View ->
                                device?.let {
                                    next(device)
                                }
                            }
                            rvDevices.adapter = deviceAdapter
                        } ?: run {
                            errorSnackbar(root, getString(R.string.api_parse_error))
                        }
                    },
                    Response.ErrorListener { error ->
                        loading.visibility = View.GONE
                        errorSnackbar(root, error.errMsg(activity, getString(R.string.api_parse_error)))
                    }
            ).apply { tag = Mirror.TAG })
        }
    }
}