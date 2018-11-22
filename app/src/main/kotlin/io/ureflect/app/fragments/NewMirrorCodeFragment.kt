package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import kotlinx.android.synthetic.main.fragment_new_mirror_code.*

@SuppressLint("ValidFragment")
class NewMirrorCodeFragment(var next: () -> Unit) : CoordinatorRootFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_new_mirror_code, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun credentialsPayloadError(): Boolean {
        return civCode.code.length != 4
    }

    private fun setupUI() {
        civCode.addOnCompleteListener {
            civCode.setEditable(true)
        }
        tvGo.setOnClickListener {
            if (!credentialsPayloadError()) {
                civCode.clearError()
                next()
            } else {
                civCode.error = getString(R.string.form_error_code_too_short)
            }
        }
    }
}