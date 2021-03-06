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
import io.ureflect.app.utils.*
import kotlinx.android.synthetic.main.activity_mirror.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

fun Context.mirrorIntent(mirror: MirrorModel): Intent = Intent(this, Mirror::class.java).apply { putExtra(MirrorModel.TAG, mirror) }

class Mirror : AppCompatActivity() {
    companion object {
        private const val EDIT_MIRROR = 12321
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

        getArg<MirrorModel>(MirrorModel.TAG)?.let {
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
                    data?.getSerializableExtra(MirrorModel.TAG)?.let {
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
                        profileAdapter = EntityAdapter(profiles, { _: ProfileModel?, _: View ->
                            startActivity(newProfileIntent(mirror))
                        }, { profile: ProfileModel?, _: View ->
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
        this.devices = ArrayList()
        fromStorage<ArrayList<ConnectedDeviceModel>>(application, ConnectedDeviceModel.LIST_TAG)?.let {
            devices = it
        }
        deviceAdapter = EntityAdapter(devices, { _: ConnectedDeviceModel?, _: View ->
            startActivity(pairDeviceIntent())
        }, { device: ConnectedDeviceModel?, _: View ->
            device?.let {
                startActivity(editDeviceIntent(device))
            }
        }, 4.5f, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt())
        rvDevices.adapter = deviceAdapter
    }

    private fun loadModules() {
        loadModulesForProfile()
    }

    private fun loadModulesForProfile(i: Int = 0, list: ArrayList<ModuleModel> = ArrayList()) {
        if (profiles.size > i) {
            loading.visibility = View.VISIBLE
            btnRetryModules.visibility = View.GONE
            queue.add(Api.Profile.one(
                    application,
                    profiles[i].ID,
                    Response.Listener { response ->
                        loading.visibility = View.GONE
                        response.data?.let { profile ->
                            profile.modules.forEach {
                                it.is_installed = true
                            }
                            list.addAll(profile.modules)
                            loadModulesForProfile(i + 1, list)
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
        } else {
            modules = ArrayList(HashSet(list)).toStorage(application, ModuleModel.LIST_TAG)
            moduleAdapter = EntityAdapter(modules, { _: ModuleModel?, _: View ->
                if (profiles.size > 0) {
                    startActivity(storeIntent(profiles[0].ID)) //TODO : ProfileID won't be necessary after next API update
                } else {
                    errorSnackbar(root, getString(R.string.need_profile_error))
                }
            }, { module: ModuleModel?, _: View ->
                if (profiles.size > 0) {
                    module?.let { startActivity(moduleIntent(module, profiles[0].ID)) } //TODO : ProfileID won't be necessary after next API update
                } else {
                    errorSnackbar(root, getString(R.string.need_profile_error))
                }
            }, 4.5f, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt())
            rvModules.adapter = moduleAdapter
        }
    }
}
