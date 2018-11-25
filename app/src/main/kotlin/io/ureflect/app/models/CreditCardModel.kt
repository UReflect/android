package io.ureflect.app.models

import java.io.Serializable

class CreditCardModel : Serializable, NamedEntity {
    private val serialVersionUID = 4L
    var id = ""
    var brand = ""
    var expMonth = 0
    var expYear = 0
    var last4 = ""
    var isClicked = false

    override fun name(): String = id
}
