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
    var owner_name: String = ""
    var your_rating: RatingModel? = null

    var is_installed = false

    override fun name(): String = title

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModuleModel

        if (ID != other.ID) return false

        return true
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    companion object {
        const val TAG = "MODULE"
        const val LIST_TAG = "MODULES"
    }
}
