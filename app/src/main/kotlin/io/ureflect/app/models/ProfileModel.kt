package io.ureflect.app.models

import java.io.Serializable

class ProfileModel : Serializable, NamedEntity {
    private val serialVersionUID = 2L
    var ID: Long = -1
    var title: String = ""
    var user_id: String = ""
    var modules: ArrayList<ModuleModel> = ArrayList()

    override fun name(): String = title
}
