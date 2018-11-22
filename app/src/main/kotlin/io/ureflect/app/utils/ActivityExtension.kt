package io.ureflect.app.utils

import android.app.Activity
import java.io.Serializable

fun <T : Serializable> Activity.getArg(identifier: String): T? {
    val args = intent.extras
    args?.getSerializable(identifier)?.let { arg ->
        return arg as T
    } ?: run {
        return null
    }
}