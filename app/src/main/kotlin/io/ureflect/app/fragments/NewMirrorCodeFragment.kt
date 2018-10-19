package io.ureflect.app.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R

class NewMirrorCodeFragment(var next: (Int) -> Unit) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_new_mirror_location, container, false)

        setupUI(rootView)

        return rootView
    }

    private fun credentialsPayloadError(v: View): Boolean {
        return false
    }

    private fun setupUI(v: View) {
        if (!credentialsPayloadError(v)) {
            //TODO : join I think
            next(1)
        }
    }
}