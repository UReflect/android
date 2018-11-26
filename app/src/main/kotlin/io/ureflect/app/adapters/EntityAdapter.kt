package io.ureflect.app.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.models.NamedEntity
import kotlinx.android.synthetic.main.view_entity.view.*

open class EntityAdapter<T : NamedEntity>(val data: List<T>,
                                          private val addListener: (T?) -> Unit,
                                          private val selectListener: (T?) -> Unit,
                                          private val nb: Float = 4.5f,
                                          private val margin: Int = 0) : RecyclerView.Adapter<EntityAdapter<T>.EntityAdapterViewHolder>() {
    companion object {
        /**
         * Number of entity icon on the screen
         */
        const val TYPE_ADD_ENTITY = 0
        const val TYPE_ENTITY = 1
    }

    override fun onBindViewHolder(holder: EntityAdapterViewHolder, position: Int) {
        when (position) {
            0 -> holder.bind(null, addListener)
            else -> holder.bind(data[position - 1], selectListener)
        }
    }

    override fun getItemCount(): Int = data.size + 1

    fun resize(parent: ViewGroup, v: View) {
        val side = ((parent.measuredWidth - margin * (nb + 1)) / nb).toInt()
        v.layoutParams = RecyclerView.LayoutParams(side, side)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityAdapterViewHolder = when (viewType) {
        TYPE_ADD_ENTITY -> AddViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_add, parent, false).apply { resize(parent, this) })
        else -> EntityViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_entity, parent, false).apply { resize(parent, this) })
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_ADD_ENTITY
        else -> TYPE_ENTITY
    }

    abstract inner class EntityAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(entity: T?, listener: (T?) -> Unit)
    }

    inner class AddViewHolder(itemView: View) : EntityAdapterViewHolder(itemView) {
        override fun bind(entity: T?, listener: (T?) -> Unit) = with(itemView) {
            setOnClickListener { listener(entity) }
        }
    }

    inner class EntityViewHolder(itemView: View) : EntityAdapterViewHolder(itemView) {
        override fun bind(entity: T?, listener: (T?) -> Unit) = with(itemView) {
            tvName.text = entity?.name()
            setOnClickListener { entity?.let { it1 -> listener(it1) } }
        }
    }
}
