package io.ureflect.app.utils

import android.app.Application
import android.preference.PreferenceManager

const val TOKEN = "token"

class Storage {
    companion object {
        fun clear(app: Application) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(app)
            val editor = preferences.edit()
            editor.clear().commit()
        }
    }
}

fun String.toStorage(app: Application, key: String): String {
    val preferences = PreferenceManager.getDefaultSharedPreferences(app)
    val editor = preferences.edit()
    editor.putString(key, this).commit()
    return this
}

fun String.Companion.fromStorage(app: Application, key: String): String {
    val preferences = PreferenceManager.getDefaultSharedPreferences(app)
    return preferences.getString(key, "")
}
