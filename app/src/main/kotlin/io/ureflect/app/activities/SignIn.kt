package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.models.ApiErrorResponse
import io.ureflect.app.services.Api
import io.ureflect.app.utils.*
import kotlinx.android.synthetic.main.activity_signin.*

fun Context.loginIntent(): Intent = Intent(this, SignIn::class.java)

class SignIn : AppCompatActivity() {
    private val TAG = "SignInActivity"
    private var triedOnce = false
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
        error = error || !evMailLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_email_required))
        error = error || !evMailLayout.validate({ s -> s.isValidEmail() }, getString(R.string.form_error_email_incorrect))
        error = error || !evPasswordLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_password_required))
        return error
    }

    private fun loginPayloadAutoValidate() {
        triedOnce = true
        evMailLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_email_required))
        evMailLayout.autoValidate({ s -> s.isValidEmail() }, getString(R.string.form_error_email_incorrect))
        evPasswordLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_password_required))
    }

    private fun setupUI() {
        btnForgotPassword.transformationMethod = null
        btnForgotPassword.setOnClickListener {}

        btnLogin.transformationMethod = null
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
