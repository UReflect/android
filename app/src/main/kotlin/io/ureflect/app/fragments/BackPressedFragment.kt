package io.ureflect.app.fragments

interface BackPressedFragment {
    fun backPressed(): Boolean

    companion object {
        val HANDLED = true
        val NOT_HANDLED = false
    }
}