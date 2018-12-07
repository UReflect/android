package io.ureflect.app.models

import java.io.Serializable

class ConnectedDeviceModel : Serializable, NamedEntity {
    private val serialVersionUID = 3L
    var ID: Long = -1
    var name: String = ""
    var description: String = ""

    override fun name(): String = name

    companion object {
        const val LIST_TAG = "Devices"
        const val TAG = "Device"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectedDeviceModel

        if (ID != other.ID) return false

        return true
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }
}
