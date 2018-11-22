package io.ureflect.app.fragments

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.view.View
import android.widget.ProgressBar
import io.ureflect.app.R
import java.io.InvalidClassException

abstract class CoordinatorRootFragment : Fragment() {
    private lateinit var view: CoordinatorLayout

    companion object {
        const val REQUIREMENT = "CoordinatorRootFragment needs a CoordinatorLayout root and a ProgressBar with id @+id/loading"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (view is CoordinatorLayout) {
            this.view = view
        } else {
            throw InvalidClassException(REQUIREMENT)
        }
    }

    fun getRoot(): CoordinatorLayout = view

    fun getLoader(): ProgressBar {
        this.view.findViewById<View>(R.id.loading)?.let {
            if (it is ProgressBar) {
                return it
            }
        }
        throw InvalidClassException(REQUIREMENT)
    }
}