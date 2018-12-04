package io.ureflect.app.fragments

interface BackPressedFragment {
    fun backPressed(): Boolean

    companion object {
        const val HANDLED = true
        const val NOT_HANDLED = false
    }
}