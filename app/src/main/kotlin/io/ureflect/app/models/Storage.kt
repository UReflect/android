package io.ureflect.app.models

import android.app.Application
import android.preference.PreferenceManager

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
