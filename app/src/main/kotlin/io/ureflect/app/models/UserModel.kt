package io.ureflect.app.models

class UserModel {
    var ID: Long = -1
    var email: String = ""
    var name: String = ""
    var password: String = ""
    var active: Boolean = false
    var email_checked: Boolean = false

    companion object {
        const val TAG = "USER"
    }
}
