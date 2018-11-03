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
import kotlinx.android.synthetic.main.fragment_new_mirror_name.*
import kotlinx.android.synthetic.main.fragment_new_mirror_name.view.*

@SuppressLint("ValidFragment")
class NewMirrorNameFragment(var next: (Int) -> Unit,
                            var setName: (String) -> Unit) : Fragment() {

    private var triedOnce = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_new_mirror_name, container, false)

        setupUI(rootView)

        return rootView
    }

    private fun credentialsPayloadError(v: View): Boolean {
        var error = false
        error = error || !v.evNameLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_name_required))
        return error
    }

    private fun credentialsPayloadAutoValidate(v: View) {
        triedOnce = true
        v.evNameLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_name_required))
    }


    private fun setupUI(v: View) {
        v.btn.setOnClickListener {
            if (!credentialsPayloadError(v)) {
                setName(evName.text.toString())
                next(1)
            } else if (!triedOnce) {
                credentialsPayloadAutoValidate(v)
            }
        }
    }
}