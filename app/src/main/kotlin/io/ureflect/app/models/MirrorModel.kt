package io.ureflect.app.models

import java.io.Serializable

class MirrorModel : Serializable {
    private val serialVersionUID = 0L
    var ID: String = ""
    var name: String = ""
    var location: String = ""
    var serial: String = ""
    var active: Boolean = false
    var joined: Boolean = false
    var joinCode: String = ""
    var timezone: String = ""
    var userID: String = ""
}
