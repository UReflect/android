package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import io.ureflect.app.R
import io.ureflect.app.adapters.ListFragmentPagerAdapter
import io.ureflect.app.fragments.CoordinatorRootFragment
import io.ureflect.app.fragments.PairingInfoFragment
import io.ureflect.app.fragments.PairingListFragment
import io.ureflect.app.models.ConnectedDeviceModel
import io.ureflect.app.services.Api
import io.ureflect.app.utils.fromStorage
import io.ureflect.app.utils.toStorage
import kotlinx.android.synthetic.main.activity_pairing.*

fun Context.pairDeviceIntent(): Intent = Intent(this, PairDevice::class.java)

class PairDevice : AppCompatActivity() {
    companion object {
        const val TAG = "PairDeviceActivity"
    }

    private var position = Steps.LIST.step
    private val fragments = ArrayList<CoordinatorRootFragment>()
    private lateinit var adapter: ListFragmentPagerAdapter
    private lateinit var device: ConnectedDeviceModel
    private lateinit var name: String

    enum class Steps(val step: Int) {
        LIST(0),
        NAME(1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing)
        Api.log("starting pairing activity")
        setupFragments()
    }

    private fun setupFragments() {
        fragments.add(
                PairingListFragment { device ->
                    this.device = device
                    next(NewMirror.Steps.NAME)
                }
        )
        fragments.add(
                PairingInfoFragment { name ->
                    this.name = name
                    saveDevice()
                }
        )
        adapter = ListFragmentPagerAdapter(supportFragmentManager, fragments)
        viewPager.adapter = adapter
        viewPager.currentItem = position
    }

    private fun saveDevice() {
        var devices: ArrayList<ConnectedDeviceModel> = ArrayList()
        fromStorage<ArrayList<ConnectedDeviceModel>>(application, ConnectedDeviceModel.LIST_TAG)?.let {
            devices = it
        }
        devices.add(device.also { it.name = this.name }.also { it.ID = devices.size.toLong() })
        devices = devices.toStorage(application, ConnectedDeviceModel.LIST_TAG)
        finish()
    }

    private fun next(step: NewMirror.Steps) {
        position = step.step
        Handler().postDelayed({ viewPager.currentItem = position }, 100)
    }

    override fun onBackPressed() {
        when (position) {
            Steps.LIST.step -> super.onBackPressed()
            else -> {
                position -= 1
                viewPager.currentItem = position
            }
        }
    }
}
