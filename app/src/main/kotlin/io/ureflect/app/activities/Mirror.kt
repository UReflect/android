package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import io.ureflect.app.R
import io.ureflect.app.adapters.EntityAdapter
import io.ureflect.app.models.ConnectedDeviceModel
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.models.ModuleModel
import io.ureflect.app.models.ProfileModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.services.expired
import io.ureflect.app.utils.EqualSpacingItemDecoration
import io.ureflect.app.utils.errorSnackbar
import io.ureflect.app.utils.getArg
import kotlinx.android.synthetic.main.activity_mirror.*
import java.text.SimpleDateFormat
import java.util.*

fun Context.mirrorIntent(mirror: MirrorModel): Intent = Intent(this, Mirror::class.java).apply { putExtra(Mirror.MIRROR, mirror) }

class Mirror : AppCompatActivity() {
    companion object {
        const val MIRROR = "mirror"
        const val TAG = "MirrorActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var mirror: MirrorModel
    private lateinit var profiles: ArrayList<ProfileModel>
    private lateinit var profileAdapter: EntityAdapter<ProfileModel>
    private lateinit var modules: ArrayList<ModuleModel>
    private lateinit var moduleAdapter: EntityAdapter<ModuleModel>
    private lateinit var devices: ArrayList<ConnectedDeviceModel>
    private lateinit var deviceAdapter: EntityAdapter<ConnectedDeviceModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mirror)
        Api.log("starting mirror activity")
        queue = Volley.newRequestQueue(this)

        getArg<MirrorModel>(MIRROR)?.let {
            this.mirror = it
        } ?: finish()

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        loadProfiles {
            loadModules()
        }
        loadDevices()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        val formatter = SimpleDateFormat("EEEE dd MMMM", Locale.getDefault())
        tvDate.text = formatter.format(Date()).toUpperCase()
        tvTitle.text = mirror.name

        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()

        rvProfiles.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvProfiles.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.HORIZONTAL))
        rvModules.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvModules.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.HORIZONTAL))
        rvDevices.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDevices.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.HORIZONTAL))

        btnRetryProfiles.transformationMethod = null
        btnRetryProfiles.setOnClickListener {
            loadProfiles {
                loadModules()
            }
        }
        btnRetryModules.transformationMethod = null
        btnRetryModules.setOnClickListener {
            loadModules()
        }
        btnRetryProfiles.transformationMethod = null
        btnRetryProfiles.setOnClickListener {
            loadDevices()
        }
    }

    private fun loadProfiles(callback: () -> Unit) {
        loading.visibility = View.VISIBLE
        btnRetryProfiles.visibility = View.GONE
        queue.add(Api.Mirror.profiles(
                this.application,
                mirror.ID,
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let { profiles ->
                        this.profiles = profiles
                        profileAdapter = EntityAdapter(profiles, {
                            startActivity(newProfileIntent(mirror))
                        }, { profile ->
                            startActivity(profile?.let { profileIntent(it) })
                        }, 4.5f, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt())
                        rvProfiles.adapter = profileAdapter
                        callback()
                    } ?: run {
                        btnRetryProfiles.visibility = View.VISIBLE
                        errorSnackbar(root, getString(R.string.api_parse_error))
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    btnRetryProfiles.visibility = View.VISIBLE
                    errorSnackbar(root, error.errMsg(getString(R.string.api_parse_error)), error.expired())
                }
        ).apply { tag = TAG })
    }

    private fun loadDevices() {
        loading.visibility = View.VISIBLE
        btnRetryDevices.visibility = View.GONE
        queue.add(Api.Misc.connectedDevices(
                this.application,
                application.assets.open("connectedDevices.json"),
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let { devices ->
                        this.devices = devices
                        deviceAdapter = EntityAdapter(devices, {
                            //                            startActivity(pairDeviceIntent())
                        }, { device ->
                            //                            startActivity(device?.let { deviceIntent(it) })
                        }, 4.5f, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt())
                        rvDevices.adapter = deviceAdapter
                    } ?: run {
                        btnRetryDevices.visibility = View.VISIBLE
                        errorSnackbar(root, getString(R.string.api_parse_error))
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    btnRetryDevices.visibility = View.VISIBLE
                    errorSnackbar(root, error.errMsg(getString(R.string.api_parse_error)), error.expired())
                }
        ).apply { tag = TAG })
    }

    private fun loadModules() {
        if (profiles.size > 0) {
            loading.visibility = View.VISIBLE
            btnRetryModules.visibility = View.GONE
            queue.add(Api.Profile.one(
                    this.application,
                    profiles[0].ID, //TODO : This is shit
                    Response.Listener { response ->
                        loading.visibility = View.GONE
                        response.data?.let { profile ->
                            this.modules = profile.modules
                            moduleAdapter = EntityAdapter(modules, {
                                //                                startActivity(installModuleIntent(mirror))
                            }, { module ->
                                //                                startActivity(module?.let { moduleIntent(it) })
                            }, 4.5f, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt())
                            rvModules.adapter = moduleAdapter
                        } ?: run {
                            btnRetryModules.visibility = View.VISIBLE
                            errorSnackbar(root, getString(R.string.api_parse_error))
                        }
                    },
                    Response.ErrorListener { error ->
                        loading.visibility = View.GONE
                        btnRetryModules.visibility = View.VISIBLE
                        errorSnackbar(root, error.errMsg(getString(R.string.api_parse_error)), error.expired())
                    }
            ).apply { tag = TAG })
        }
    }
}
