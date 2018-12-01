package io.ureflect.app.adapters

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import io.ureflect.app.R
import io.ureflect.app.models.CommentModel
import kotlinx.android.synthetic.main.view_entity.view.*

class CommentAdapter(data: List<CommentModel>, addListener: (CommentModel?, View) -> Unit) : EntityAdapter<CommentModel>(data, addListener, { _: CommentModel?, _ -> }) {

    private fun changeBackground(v: View) = v.setBackgroundColor(ResourcesCompat.getColor(v.context.resources, android.R.color.transparent, null))

    private fun resize(v: View) = v.apply { layoutParams = RecyclerView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) }

    private fun unCenter(v: View) = v.tvName.apply { textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityAdapterViewHolder = when (viewType) {
        EntityAdapter.TYPE_ADD_ENTITY -> AddViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_add, parent, false))
        else -> EntityViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_entity, parent, false)
                .apply { resize(this) }
                .apply { changeBackground(this) }
                .apply { unCenter(this) }
        )
    }
}