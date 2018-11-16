package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import io.ureflect.app.R
import io.ureflect.app.adapters.EntityAdapter
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.models.ProfileModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.utils.EqualSpacingItemDecoration
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mirror)
        Api.log("starting mirror activity")
        queue = Volley.newRequestQueue(this)

        val args = intent.extras
        args?.getSerializable(MIRROR)?.let { mirror ->
            this.mirror = mirror as MirrorModel
        } ?: run {
            finish()
        }

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        loadProfiles()
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

        btnRetryProfiles.transformationMethod = null
        btnRetryProfiles.setOnClickListener {
            loadProfiles()
        }
    }

    private fun loadProfiles() {
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
                    } ?: run {
                        btnRetryProfiles.visibility = View.VISIBLE
                        Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    btnRetryProfiles.visibility = View.VISIBLE
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ).apply { tag = TAG })
    }

}
