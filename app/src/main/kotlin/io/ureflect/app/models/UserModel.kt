package io.ureflect.app.models

import android.annotation.SuppressLint
import android.app.Application
import android.preference.PreferenceManager
import com.google.gson.Gson

class UserModel {
    var ID: Long = -1
    var email: String = ""
    var name: String = ""
    var password: String = ""
    var active: Boolean = false
    var email_checked: Boolean = false

    @SuppressLint("ApplySharedPref")
    fun toStorage(app: Application): UserModel {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putString(TAG, Gson().toJson(this))
                .commit()
        return this
    }

    companion object {
        const val TAG = "USER"

        fun fromStorage(app: Application): UserModel = Gson().fromJson<UserModel>(
                PreferenceManager.getDefaultSharedPreferences(app).getString(TAG, ""),
                UserModel::class.java
        )
    }
}
