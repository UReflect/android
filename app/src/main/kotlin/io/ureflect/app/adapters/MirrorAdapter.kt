package io.ureflect.app.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.models.MirrorModel
import kotlinx.android.synthetic.main.view_mirror.view.*

class MirrorAdapter(val data: List<MirrorModel>, private val addListener: (MirrorModel) -> Unit, private val selectListener: (MirrorModel) -> Unit) : RecyclerView.Adapter<MirrorAdapter.MirrorAdapterViewHolder>() {
    /**
     * Number of mirror icon on the screen
     */
    private val NB = 4.5
    private val TYPE_ADD_MIRROR = 0
    private val TYPE_MIRROR = 1


    override fun onBindViewHolder(holder: MirrorAdapterViewHolder, position: Int) = when (position) {
        0 -> holder.bind(MirrorModel(), addListener)
        else -> holder.bind(data[position - 1], selectListener)
    }

    override fun getItemCount(): Int = data.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MirrorAdapterViewHolder {
        val v: View = when (viewType) {
            TYPE_ADD_MIRROR -> {
                LayoutInflater.from(parent.context).inflate(R.layout.view_add_mirror, parent, false)
            }
            TYPE_MIRROR -> {
                LayoutInflater.from(parent.context).inflate(R.layout.view_mirror, parent, false)
            }
            else -> LayoutInflater.from(parent.context).inflate(R.layout.view_mirror, parent, false)
        }

        val side = (parent.measuredWidth / NB).toInt()
        v.layoutParams = RecyclerView.LayoutParams(side, side)

        return when (viewType) {
            TYPE_ADD_MIRROR -> {
                AddMirrorViewHolder(v)
            }
            TYPE_MIRROR -> {
                MirrorViewHolder(v)
            }
            else -> MirrorViewHolder(v)
        }
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_ADD_MIRROR
        else -> TYPE_MIRROR
    }

    abstract class MirrorAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(mirror: MirrorModel, listener: (MirrorModel) -> Unit)
    }

    class AddMirrorViewHolder(itemView: View) : MirrorAdapterViewHolder(itemView) {
        override fun bind(mirror: MirrorModel, listener: (MirrorModel) -> Unit) = with(itemView) {
            setOnClickListener { listener(mirror) }
        }
    }

    class MirrorViewHolder(itemView: View) : MirrorAdapterViewHolder(itemView) {
        override fun bind(mirror: MirrorModel, listener: (MirrorModel) -> Unit) = with(itemView) {
            tvName.text = mirror.name
            setOnClickListener { listener(mirror) }
        }
    }
}
