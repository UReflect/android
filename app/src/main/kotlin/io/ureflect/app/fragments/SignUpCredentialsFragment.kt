package io.ureflect.app.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.utils.autoValidate
import io.ureflect.app.utils.isValidEmail
import io.ureflect.app.utils.validate
import io.ureflect.app.R
import kotlinx.android.synthetic.main.fragment_signup_credentials.*
import kotlinx.android.synthetic.main.fragment_signup_credentials.view.*

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
        error = error || !v.evMailLayout.validate({ s -> s.isNotEmpty() }, "Email obligatoire")
        error = error || !v.evMailLayout.validate({ s -> s.isValidEmail() }, "Email incorrect")
        error = error || !v.evPasswordLayout.validate({ s -> s.isNotEmpty() }, "Mot de passe obligatoire")
        return error
    }

    private fun credentialsPayloadAutoValidate(v: View) {
        triedOnce = true
        v.evMailLayout.autoValidate({ s -> s.isNotEmpty() }, "Email obligatoire")
        v.evMailLayout.autoValidate({ s -> s.isValidEmail() }, "Email incorrect")
        v.evPasswordLayout.autoValidate({ s -> s.isNotEmpty() }, "Mot de passe obligatoire")
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