package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.services.Api
import io.ureflect.app.R
import kotlinx.android.synthetic.main.activity_signin.*

fun Context.loginIntent(): Intent = Intent(this, Login::class.java)

class Login : AppCompatActivity() {
    val TAG = "LoginActivity"
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        Api.log("starting login activity")
        queue = Volley.newRequestQueue(this)
        setupUI()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        btnForgotPassword.setOnClickListener {}

        btnLogin.setOnClickListener { _ ->
            val data = JsonObject()
            data.addProperty("email", evMail.text.toString().toLowerCase())
            data.addProperty("password", evPassword.text.toString())
            queue.add(Api.signin(
                    data,
                    Response.Listener { response ->
                        tvResult.text = response.data?.token
                    },
                    Response.ErrorListener {
                        Log.e(TAG, "That didn't work!")
                    }
            ))
        }
    }
}
