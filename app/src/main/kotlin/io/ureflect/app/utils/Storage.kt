package io.ureflect.app.utils

import android.annotation.SuppressLint
import android.app.Application
import android.preference.PreferenceManager

const val TOKEN = "token"

class Storage {
    companion object {
        fun clear(app: Application) = PreferenceManager.getDefaultSharedPreferences(app).edit().clear().commit()
    }
}

@SuppressLint("ApplySharedPref")
fun String.toStorage(app: Application, key: String): String {
    PreferenceManager.getDefaultSharedPreferences(app).edit().putString(key, this).commit()
    return this
}

fun String.Companion.fromStorage(app: Application, key: String): String = PreferenceManager.getDefaultSharedPreferences(app).getString(key, "")
