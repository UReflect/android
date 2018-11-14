package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.utils.autoValidate
import io.ureflect.app.utils.validate
import kotlinx.android.synthetic.main.fragment_signup_identity.*

@SuppressLint("ValidFragment")
class SignUpIdentityFragment(val next: () -> Unit,
                             var setFirstname: (String) -> Unit,
                             var setLastname: (String) -> Unit) : Fragment() {
    private var triedOnce = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_signup_identity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun identityPayloadError(): Boolean {
        var error = false
        error = error || !evFirstnameLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_first_name_required))
        error = error || !evLastnameLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_last_name_required))
        return error
    }

    private fun identityPayloadAutoValidate() {
        triedOnce = true
        evFirstnameLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_first_name_required))
        evLastnameLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_last_name_required))
    }

    private fun setupUI() {
        btn.transformationMethod = null
        btn.setOnClickListener {
            if (!identityPayloadError()) {
                setFirstname(evFirstname.text.toString())
                setLastname(evLastname.text.toString())
                next()
            } else if (!triedOnce) {
                identityPayloadAutoValidate()
            }
        }
    }
}