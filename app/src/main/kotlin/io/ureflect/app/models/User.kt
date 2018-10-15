package io.ureflect.app.models

import android.app.Application
import android.preference.PreferenceManager
import com.google.gson.Gson

class User {
    var ID: Long = -1
    var email: String = ""
    var name: String = ""
    var active: Boolean = false
    var email_checked: Boolean = false

    fun toStorage(app: Application): User {
        val preferences = PreferenceManager.getDefaultSharedPreferences(app)
        val editor = preferences.edit()
        val output = Gson().toJson(this)
        editor.putString(TAG, output).commit()
        return this
    }

    companion object {
        const val TAG = "USER"

        fun fromStorage(app: Application): User {
            val preferences = PreferenceManager.getDefaultSharedPreferences(app)
            return Gson().fromJson<User>(preferences.getString(TAG, ""), User::class.java)
        }
    }
}
