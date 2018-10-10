package io.ureflect.app.models

class User {
    var ID: Long = -1
    lateinit var email: String
    lateinit var name: String
    var active: Boolean = false
    var email_checked: Boolean = false
}
