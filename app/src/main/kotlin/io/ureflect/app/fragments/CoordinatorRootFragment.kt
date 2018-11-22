package io.ureflect.app.fragments

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.view.View
import android.widget.ProgressBar
import io.ureflect.app.R

abstract class CoordinatorRootFragment : Fragment() {
    private lateinit var view: CoordinatorLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.view = view as CoordinatorLayout
    }

    fun getRoot(): CoordinatorLayout = view

    fun getLoader(): ProgressBar = view.findViewById(R.id.loading)
}