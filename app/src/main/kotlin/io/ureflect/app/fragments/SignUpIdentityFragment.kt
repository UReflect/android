package io.ureflect.app.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.utils.autoValidate
import io.ureflect.app.utils.validate
import io.ureflect.app.R
import kotlinx.android.synthetic.main.fragment_signup_identity.*
import kotlinx.android.synthetic.main.fragment_signup_identity.view.*

class SignUpIdentityFragment(val next: (Int) -> Unit,
                             var setFirstname: (String) -> Unit,
                             var setLastname : (String) -> Unit) : Fragment() {
    var triedOnce = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_signup_identity, container, false)

        setupUI(rootView)

        return rootView
    }

    private fun identityPayloadError(v: View): Boolean {
        var error = false
        error = error || !v.evFirstnameLayout.validate({ s -> s.isNotEmpty() }, "Prenom obligatoire")
        error = error || !v.evLastnameLayout.validate({ s -> s.isNotEmpty() }, "Nom obligatoire")
        return error
    }

    private fun identityPayloadAutoValidate(v: View) {
        triedOnce = true;
        v.evFirstnameLayout.autoValidate({ s -> s.isNotEmpty() }, "Prenom obligatoire")
        v.evLastnameLayout.autoValidate({ s -> s.isNotEmpty() }, "Nom obligatoire")
    }

    private fun setupUI(v: View) {
        v.btn.setOnClickListener { _ ->
            if (!identityPayloadError(v)) {
                setFirstname(evFirstname.text.toString())
                setLastname(evLastname.text.toString())
                next(0)
            } else if (triedOnce) {
                identityPayloadAutoValidate(v)
            }
        }
    }
}