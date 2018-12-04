package io.ureflect.app.models

import java.io.Serializable

class CommentModel : Serializable, NamedEntity {
    private val serialVersionUID = 5L
    var ID: Long = -1
    var value: String = ""

    override fun name(): String = value
}
