package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.utils.autoValidate
import io.ureflect.app.utils.isValidEmail
import io.ureflect.app.utils.validate
import kotlinx.android.synthetic.main.fragment_signup_credentials.*

@SuppressLint("ValidFragment")
class SignUpCredentialsFragment(var next: (String, String) -> Unit) : CoordinatorRootFragment() {

    private var triedOnce = false

    //TODO : request here for move upward behavior

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_signup_credentials, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun credentialsPayloadError(): Boolean {
        var error = false
        error = error || !evMailLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_email_required))
        error = error || !evMailLayout.validate({ s -> s.isValidEmail() }, getString(R.string.form_error_email_incorrect))
        error = error || !evPasswordLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_password_required))
        return error
    }

    private fun credentialsPayloadAutoValidate() {
        triedOnce = true
        evMailLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_email_required))
        evMailLayout.autoValidate({ s -> s.isValidEmail() }, getString(R.string.form_error_email_incorrect))
        evPasswordLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_password_required))
    }

    private fun setupUI() {
        btn.setOnClickListener {
            if (!credentialsPayloadError()) {
                next(evMail.text.toString(), evPassword.text.toString())
            } else if (!triedOnce) {
                credentialsPayloadAutoValidate()
            }
        }
    }
}