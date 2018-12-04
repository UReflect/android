package io.ureflect.app.models

import java.io.Serializable

class MirrorModel : Serializable, NamedEntity {
    private val serialVersionUID = 0L
    var ID: Long = -1
    var name: String = ""
    var location: String = ""
    var serial: String = ""
    var active: Boolean = false
    var joined: Boolean = false
    var joinCode: String = ""
    var timezone: String = ""
    var userID: String = ""

    override fun name(): String = name

    companion object {
        const val TAG = "MIRROR"
    }
}
