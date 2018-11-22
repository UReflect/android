package io.ureflect.app.models

import java.io.Serializable

class ConnectedDeviceModel : Serializable, NamedEntity {
    private val serialVersionUID = 4L
    var ID: Long = -1
    var name: String = ""

    override fun name(): String = name
}