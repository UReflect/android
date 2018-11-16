package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.utils.*
import kotlinx.android.synthetic.main.activity_signin.*

fun Context.loginIntent(): Intent = Intent(this, SignIn::class.java)

class SignIn : AppCompatActivity() {
    companion object {
        const val TAG = "SignInActivity"
    }

    private var triedOnce = false
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        Api.log("starting login activity")
        queue = Volley.newRequestQueue(this)
        queue
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
        btnLogin.setOnClickListener {
            if (!loginPayloadError()) {
                queue.add(Api.Auth.signin(
                        JsonObject().apply { addProperty("email", evMail.text.toString().toLowerCase()) }
                                .apply { addProperty("password", evPassword.text.toString()) },
                        Response.Listener { response ->
                            val user = response.data?.user?.toStorage(this.application)
                            val token = response.data?.token?.toStorage(this.application, TOKEN)
                            if (user == null || token == null) {
                                Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                                return@Listener
                            }
                            toHomeView()
                        },
                        Response.ErrorListener { error ->
                            Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                        }
                ).apply { tag = TAG })
            } else if (triedOnce) {
                loginPayloadAutoValidate()
            }
        }
    }
}
