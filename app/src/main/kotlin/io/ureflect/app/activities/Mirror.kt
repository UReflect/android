package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.ureflect.app.R
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.services.Api
import kotlinx.android.synthetic.main.activity_mirror.*
import java.text.SimpleDateFormat
import java.util.*

fun Context.mirrorIntent(mirror: MirrorModel): Intent = Intent(this, Mirror::class.java).apply { putExtra(Mirror.MIRROR, mirror) }

class Mirror : AppCompatActivity() {
    companion object {
        val MIRROR = "mirror"
    }

    private val TAG = "MirrorActivity"
    private lateinit var queue: RequestQueue
    private lateinit var mirror: MirrorModel

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

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        val formatter = SimpleDateFormat("EEEE dd MMMM", Locale.getDefault())
        tvDate.text = formatter.format(Date()).toUpperCase()
        tvTitle.text = mirror.name
        //TMP
        cvLogo.setOnClickListener {
            startActivity(newProfileIntent())
        }
    }
}
