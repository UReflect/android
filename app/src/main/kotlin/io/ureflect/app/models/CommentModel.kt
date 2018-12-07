package io.ureflect.app.models

import java.io.Serializable

class CommentModel : Serializable, NamedEntity {
    private val serialVersionUID = 5L
    var ID: Long = -1
    var value: String = ""
    var owner_name: String = ""

    override fun name(): String = "$owner_name :\n$value"
}
