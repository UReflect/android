package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.ureflect.app.services.Api
import io.ureflect.app.R
import io.ureflect.app.models.User
import kotlinx.android.synthetic.main.activity_home.*
import java.text.SimpleDateFormat
import java.util.*

fun Context.homeIntent(): Intent = Intent(this, Home::class.java)

class Home : AppCompatActivity() {
    val TAG = "HomeActivity"
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Api.log("starting home activity")
        queue = Volley.newRequestQueue(this)
        setupUI()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        val formater = SimpleDateFormat("EEEE dd MMMM", Locale.FRANCE)
        tvDate.text = formater.format(Date()).toUpperCase()
        val user = User.fromStorage(this.application)
        tvTitle.text = getString(R.string.home_title_text, user.name)
    }
}
