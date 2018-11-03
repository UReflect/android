package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.ureflect.app.models.Mirror
import io.ureflect.app.services.Api

fun Context.mirrorIntent(mirror: Mirror): Intent {
    val intent = Intent(this, Mirror::class.java)
    intent.putExtra("mirror", mirror)
    return intent
}

class Mirror : AppCompatActivity() {
    private val TAG = "MirrorActivity"
    private lateinit var queue: RequestQueue
    private lateinit var mirror: Mirror

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_mirror)
        Api.log("starting mirror activity")
        queue = Volley.newRequestQueue(this)
        setupUI()

        val args = intent.extras
        mirror = args.getSerializable("mirror") as Mirror
        if (mirror == null) {
            finish()
            return
        }
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
    }
}
