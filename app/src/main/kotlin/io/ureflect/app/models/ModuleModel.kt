package io.ureflect.app.models

import java.io.Serializable

class ModuleModel : Serializable, NamedEntity {
    private val serialVersionUID = 1L
    var ID: Long = -1
    var name: String = ""

    override fun name(): String = name
}
