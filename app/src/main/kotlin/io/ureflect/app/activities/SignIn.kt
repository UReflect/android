package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.models.UserModel
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
        btnForgotPassword.setOnClickListener {
            val input: EditText = layoutInflater.inflate(R.layout.view_new_comment, null) as EditText
            AlertDialog.Builder(this)
                    .apply { title = getString(R.string.form_text_email) }
                    .apply { setMessage(R.string.enter_email_text) }
                    .apply { setView(input) }
                    .apply {
                        setPositiveButton(getString(R.string.ok_btn_text)) { _, _ ->
                            val value = input.text
                            if (!value.isEmpty()) {
                                forgotPassword(value.toString())
                            }
                        }
                    }
                    .apply { setNegativeButton(getString(R.string.cancel_btn_text)) { _, _ -> } }
                    .apply { show() }
        }

        btnLogin.transformationMethod = null
        btnLogin.setOnClickListener {
            signIn()
        }
    }

    private fun forgotPassword(email: String) {
        loading.visibility = View.VISIBLE
        queue.add(Api.Auth.lost(
                JsonObject().apply { addProperty("email", email.toLowerCase().trim()) },
                Response.Listener {
                    loading.visibility = View.INVISIBLE
                    successSnackbar(root, getString(R.string.check_email_text))
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.INVISIBLE
                    errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                }
        ).apply { tag = TAG })
    }

    private fun signIn() {
        if (!loginPayloadError()) {
            loading.visibility = View.VISIBLE
            queue.add(Api.Auth.signin(
                    JsonObject().apply { addProperty("email", evMail.text.toString().toLowerCase()) }
                            .apply { addProperty("password", evPassword.text.toString()) },
                    Response.Listener { response ->
                        loading.visibility = View.INVISIBLE
                        val user = response.data?.user?.apply { password = evPassword.text.toString() }?.toStorage(application, UserModel.TAG)
                        val token = response.data?.token?.toStorage(application, TOKEN)
                        if (user == null || token == null) {
                            Storage.clear(application)
                            errorSnackbar(root, getString(R.string.api_parse_error))
                            return@Listener
                        }
                        toHomeView()
                    },
                    Response.ErrorListener { error ->
                        loading.visibility = View.INVISIBLE
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
            ).apply { tag = TAG })
        } else if (triedOnce) {
            loginPayloadAutoValidate()
        }
    }
}
