package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.services.Api
import io.ureflect.app.R
import io.ureflect.app.models.toStorage
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

    private fun toHomeView() {
        startActivity(homeIntent())
        finish()
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
                        val user = response.data?.user?.toStorage(this.application)
                        val token = response.data?.token?.toStorage(this.application, "token")
                        if (user == null || token == null) {
                            tvError.text = getString(R.string.api_parse_error)
                            return@Listener
                        }
                        toHomeView()
                    },
                    Response.ErrorListener {error ->
                        tvError.text = String(error.networkResponse.data)
                    }
            ))
        }
    }
}
