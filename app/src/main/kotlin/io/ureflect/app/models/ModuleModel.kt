package io.ureflect.app.models

import java.io.Serializable

class ModuleModel : Serializable, NamedEntity {
    private val serialVersionUID = 1L
    var ID: Long = -1
    var title: String = ""
    var description: String = ""
    var price: Double = 0.0
    var rating: Double = 0.0
    var rating_nb: Int = 0

    override fun name(): String = title
}
