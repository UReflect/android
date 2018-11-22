package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.utils.autoValidate
import io.ureflect.app.utils.validate
import kotlinx.android.synthetic.main.fragment_new_mirror_location.*

@SuppressLint("ValidFragment")
class NewMirrorLocationFragment(var next: (String) -> Unit) : CoordinatorRootFragment() {
    private var triedOnce = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_new_mirror_location, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun credentialsPayloadError(): Boolean {
        var error = false
        error = error || !evLocationLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_location_required))
        return error
    }

    private fun credentialsPayloadAutoValidate() {
        triedOnce = true
        evLocationLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_location_required))
    }


    private fun setupUI() {
        btn.setOnClickListener {
            if (!credentialsPayloadError()) {
                next(evLocation.text.toString())
            } else if (!triedOnce) {
                credentialsPayloadAutoValidate()
            }
        }
    }
}