package io.ureflect.app.adapters

import android.annotation.SuppressLint
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import io.ureflect.app.R
import io.ureflect.app.models.ConnectedDeviceModel
import kotlinx.android.synthetic.main.view_entity.view.*

class DeviceAdapter(data: List<ConnectedDeviceModel>, private val selectListener: (ConnectedDeviceModel?, View) -> Unit) : EntityAdapter<ConnectedDeviceModel>(data, { _, _ -> }, selectListener) {

    private fun changeBackground(v: View) = v.setBackgroundColor(ResourcesCompat.getColor(v.context.resources, android.R.color.transparent, null))

    private fun resize(v: View) = v.apply { layoutParams = RecyclerView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) }

    private fun biggerText(v: View) = v.tvName.apply { textSize = 16.0f }

    private fun unCenter(v: View) = v.tvName.apply { textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityAdapterViewHolder = ConnectedDeviceAdapterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_entity, parent, false)
            .apply { changeBackground(this) }
            .apply { unCenter(this) }
            .apply { biggerText(this) }
            .apply { resize(this) }
    )

    override fun onBindViewHolder(holder: EntityAdapterViewHolder, position: Int) {
        holder.bind(data[position], selectListener)
    }

    override fun getItemCount(): Int = data.size

    inner class ConnectedDeviceAdapterViewHolder(itemView: View) : EntityAdapterViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        override fun bind(entity: ConnectedDeviceModel?, listener: (ConnectedDeviceModel?, View) -> Unit) = with(itemView) {
            tvName.text = entity?.name + " - " + entity?.description
            setOnClickListener {
                entity?.let { card ->
                    listener(card, this)
                }
            }
        }
    }
}