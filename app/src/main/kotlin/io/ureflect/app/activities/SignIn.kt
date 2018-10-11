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
import kotlinx.android.synthetic.main.activity_signin.*
import com.google.gson.Gson
import io.ureflect.app.models.ApiErrorResponse
import io.ureflect.app.utils.*

fun Context.loginIntent(): Intent = Intent(this, Login::class.java)

class Login : AppCompatActivity() {
    val TAG = "SignInActivity"
    var triedOnce = false
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

    private fun loginPayloadError(): Boolean {
        var error = false
        error = error || !evMailLayout.validate({ s -> s.isNotEmpty() }, "Email obligatoire")
        error = error || !evMailLayout.validate({ s -> s.isValidEmail() }, "Email incorrect")
        error = error || !evPasswordLayout.validate({ s -> s.isNotEmpty() }, "Mot de passe obligatoire")
        return error
    }

    private fun loginPayloadAutoValidate() {
        triedOnce = true;
        evMailLayout.autoValidate({ s -> s.isNotEmpty() }, "Email obligatoire")
        evMailLayout.autoValidate({ s -> s.isValidEmail() }, "Email incorrect")
        evPasswordLayout.autoValidate({ s -> s.isNotEmpty() }, "Mot de passe obligatoire")
    }

    private fun setupUI() {
        btnForgotPassword.setOnClickListener {}
        btnLogin.setOnClickListener { _ ->
            if (!loginPayloadError()) {
                val data = JsonObject()
                data.addProperty("email", evMail.text.toString().toLowerCase())
                data.addProperty("password", evPassword.text.toString())
                queue.add(Api.signin(
                        data,
                        Response.Listener { response ->
                            val user = response.data?.user?.toStorage(this.application)
                            val token = response.data?.token?.toStorage(this.application, TOKEN)
                            if (user == null || token == null) {
                                tvError.text = getString(R.string.api_parse_error)
                                return@Listener
                            }
                            toHomeView()
                        },
                        Response.ErrorListener { error ->
                            val errorResponse = Gson().fromJson(String(error.networkResponse.data), ApiErrorResponse::class.java)
                            tvError.text = errorResponse.error
                        }
                ))
            } else if (triedOnce) {
                loginPayloadAutoValidate()
            }
        }
    }
}
