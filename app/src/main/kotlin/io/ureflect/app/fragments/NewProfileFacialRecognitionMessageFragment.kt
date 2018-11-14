package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import kotlinx.android.synthetic.main.fragment_new_profile_facial_msg.*

@SuppressLint("ValidFragment")
class NewProfileFacialRecognitionMessageFragment(var next: () -> Unit, val configure: () -> Unit) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_new_profile_facial_msg, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        btnConfigure.transformationMethod = null
        btnConfigure.setOnClickListener {
            configure()
        }
        btnSkip.transformationMethod = null
        btnSkip.setOnClickListener {
            next()
        }
    }
}