package io.ureflect.app.utils

import android.annotation.SuppressLint
import android.app.Application
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.Gson
import io.ureflect.app.services.Api

const val TOKEN = "token"

class Storage {
    companion object {
        fun clear(app: Application) = PreferenceManager.getDefaultSharedPreferences(app).edit().clear().commit()
    }
}

@SuppressLint("ApplySharedPref")
fun <T> T.toStorage(app: Application, key: String): T = this.apply {
    PreferenceManager.getDefaultSharedPreferences(app)
            .edit()
            .putString(key, Gson().toJson(this))
            .commit()
}

inline fun <reified T> fromStorage(app: Application, key: String): T? = try {
    Log.i("Storage", "Retrieved " + key + ": " + PreferenceManager.getDefaultSharedPreferences(app).getString(key, "null"))
    Gson().fromJson(PreferenceManager.getDefaultSharedPreferences(app).getString(key, "null"), Api.genericType<T>())
} catch (e: Exception) {
    null
}