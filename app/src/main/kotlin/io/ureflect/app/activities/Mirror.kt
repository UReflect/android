package io.ureflect.app.activities

import android.app.Activity
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
import io.ureflect.app.services.isExpired
import io.ureflect.app.utils.EqualSpacingItemDecoration
import io.ureflect.app.utils.errorSnackbar
import io.ureflect.app.utils.getArg
import io.ureflect.app.utils.reLogin
import kotlinx.android.synthetic.main.activity_mirror.*
import java.text.SimpleDateFormat
import java.util.*

fun Context.mirrorIntent(mirror: MirrorModel): Intent = Intent(this, Mirror::class.java).apply { putExtra(Mirror.MIRROR, mirror) }

class Mirror : AppCompatActivity() {
    companion object {
        private const val EDIT_MIRROR = 12321
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
            mirror = it
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
        setupMirror()

        tvName.setOnClickListener {
            toEditMirror()
        }
        tvLocation.setOnClickListener {
            toEditMirror()
        }
        tvTimezone.setOnClickListener {
            toEditMirror()
        }

        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()

        rvProfiles.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvProfiles.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.HORIZONTAL))
        rvModules.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvModules.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.HORIZONTAL))
        rvDevices.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDevices.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.HORIZONTAL))

        btnRetryProfiles.setOnClickListener {
            loadProfiles {
                loadModules()
            }
        }

        btnRetryModules.setOnClickListener {
            if (::profiles.isInitialized) {
                loadModules()
            } else {
                loadProfiles {
                    loadModules()
                }
            }
        }

        btnRetryDevices.setOnClickListener {
            loadDevices()
        }
    }

    private fun setupMirror() {
        tvTitle.text = mirror.name
        tvNameDetails.text = mirror.name
        tvLocationDetails.text = mirror.location
        tvTimezoneDetails.text = mirror.timezone
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EDIT_MIRROR -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.getSerializableExtra(MIRROR)?.let {
                        if (it is MirrorModel) {
                            mirror = it
                            setupMirror()
                        }
                    }
                }
            }
            else -> errorSnackbar(root, getString(R.string.generic_error))
        }
    }

    private fun toEditMirror() = startActivityForResult(editMirrorIntent(mirror), EDIT_MIRROR)

    private fun loadProfiles(callback: () -> Unit) {
        loading.visibility = View.VISIBLE
        btnRetryProfiles.visibility = View.GONE
        queue.add(Api.Mirror.profiles(
                application,
                mirror.ID,
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let { profiles ->
                        this.profiles = profiles
                        profileAdapter = EntityAdapter(profiles, {
                            startActivity(newProfileIntent(mirror))
                        }, { profile ->
                            startActivity(profile?.let { editProfileIntent(it) })
                        }, 4.5f, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt())
                        rvProfiles.adapter = profileAdapter
                        callback()
                    } ?: run {
                        btnRetryProfiles.visibility = View.VISIBLE
                        btnRetryModules.visibility = View.VISIBLE
                        errorSnackbar(root, getString(R.string.api_parse_error))
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    btnRetryProfiles.visibility = View.VISIBLE
                    btnRetryModules.visibility = View.VISIBLE
                    if (error.isExpired()) {
                        reLogin(loading, root, queue) {
                            loadProfiles(callback)
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = TAG })
    }

    private fun loadDevices() {
        loading.visibility = View.VISIBLE
        btnRetryDevices.visibility = View.GONE
        queue.add(Api.Misc.connectedDevices(
                application,
                application.assets.open("connectedDevices.json"),
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let { devices ->
                        this.devices = devices
                        deviceAdapter = EntityAdapter(devices, {
                            //TODO :
                            //                            startActivity(pairDeviceIntent())
                        }, { device ->
                            //TODO :
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
                    if (error.isExpired()) {
                        reLogin(loading, root, queue) {
                            loadDevices()
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = TAG })
    }

    private fun loadModules() {
        if (profiles.size > 0) {
            loading.visibility = View.VISIBLE
            btnRetryModules.visibility = View.GONE
            queue.add(Api.Profile.one(
                    application,
                    profiles[0].ID, //TODO : This is shit
                    Response.Listener { response ->
                        loading.visibility = View.GONE
                        response.data?.let { profile ->
                            modules = profile.modules
                            moduleAdapter = EntityAdapter(modules, {
                                //TODO :
                                //                                startActivity(installModuleIntent(mirror))
                            }, { module ->
                                //TODO :
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
                        if (error.isExpired()) {
                            reLogin(loading, root, queue) {
                                loadModules()
                            }
                        } else {
                            errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                        }
                    }
            ).apply { tag = TAG })
        }
    }
}
