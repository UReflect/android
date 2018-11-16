package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.ureflect.app.R
import io.ureflect.app.models.ProfileModel
import io.ureflect.app.services.Api
import kotlinx.android.synthetic.main.activity_profile.*
import java.text.SimpleDateFormat
import java.util.*

fun Context.profileIntent(profile: ProfileModel): Intent = Intent(this, Profile::class.java).apply { putExtra(Profile.PROFILE, profile) }

class Profile : AppCompatActivity() {
    companion object {
        const val PROFILE = "Profile"
        const val TAG = "ProfileActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var profile: ProfileModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Api.log("starting Profile activity")
        queue = Volley.newRequestQueue(this)

        val args = intent.extras
        args?.getSerializable(PROFILE)?.let { Profile ->
            this.profile = Profile as ProfileModel
        } ?: run {
            finish()
        }

        setupUI()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        tvTitle.text = profile.title
    }
}
