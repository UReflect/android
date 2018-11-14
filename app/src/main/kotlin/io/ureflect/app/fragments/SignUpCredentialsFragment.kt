package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.utils.autoValidate
import io.ureflect.app.utils.isValidEmail
import io.ureflect.app.utils.validate
import kotlinx.android.synthetic.main.fragment_signup_credentials.*

@SuppressLint("ValidFragment")
class SignUpCredentialsFragment(var next: () -> Unit,
                                var setMail: (String) -> Unit,
                                var setPassword: (String) -> Unit) : Fragment() {

    private var triedOnce = false

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
                setMail(evMail.text.toString())
                setPassword(evPassword.text.toString())
                next()
            } else if (!triedOnce) {
                credentialsPayloadAutoValidate()
            }
        }
    }
}