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
import kotlinx.android.synthetic.main.fragment_signup_credentials.view.*

@SuppressLint("ValidFragment")
class SignUpCredentialsFragment(var next: (Int) -> Unit,
                                var setMail: (String) -> Unit,
                                var setPassword: (String) -> Unit) : Fragment() {

    private var triedOnce = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_signup_credentials, container, false)

        setupUI(rootView)

        return rootView
    }

    private fun credentialsPayloadError(v: View): Boolean {
        var error = false
        error = error || !v.evMailLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_email_required))
        error = error || !v.evMailLayout.validate({ s -> s.isValidEmail() }, getString(R.string.form_error_email_incorrect))
        error = error || !v.evPasswordLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_password_required))
        return error
    }

    private fun credentialsPayloadAutoValidate(v: View) {
        triedOnce = true
        v.evMailLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_email_required))
        v.evMailLayout.autoValidate({ s -> s.isValidEmail() }, getString(R.string.form_error_email_incorrect))
        v.evPasswordLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_password_required))
    }


    private fun setupUI(v: View) {
        v.btn.setOnClickListener { _ ->
            if (!credentialsPayloadError(v)) {
                setMail(evMail.text.toString())
                setPassword(evPassword.text.toString())
                next(1)
            } else if (!triedOnce) {
                credentialsPayloadAutoValidate(v)
            }
        }
    }
}