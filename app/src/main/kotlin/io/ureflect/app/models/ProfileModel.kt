package io.ureflect.app.models

import java.io.Serializable

class ProfileModel : Serializable {
    private val serialVersionUID = 1L
    var ID: Long = -1
    var email: String = ""
    var name: String = ""
    var active: Boolean = false
    var email_checked: Boolean = false
}
