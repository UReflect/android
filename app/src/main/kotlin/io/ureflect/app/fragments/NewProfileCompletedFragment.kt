package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import kotlinx.android.synthetic.main.fragment_new_profile_completed.*

@SuppressLint("ValidFragment")
class NewProfileCompletedFragment(var next: () -> Unit) : CoordinatorRootFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_new_profile_completed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        btn.transformationMethod = null
        btn.setOnClickListener {
            next()
        }
    }
}