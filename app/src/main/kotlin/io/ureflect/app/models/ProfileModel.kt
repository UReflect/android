package io.ureflect.app.models

import java.io.Serializable

class ProfileModel : Serializable, NamedEntity {
    private val serialVersionUID = 1L
    var ID: Long = -1
    var title: String = ""
    var user_id: String = ""

    override fun name(): String = title
//    var modules:
}
