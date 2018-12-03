package io.ureflect.app.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.models.ModuleModel
import kotlinx.android.synthetic.main.view_module.view.*

class ModuleAdapter(val data: List<ModuleModel>, private val viewListener: (ModuleModel?, View) -> Unit, private val installListener: (ModuleModel?, View) -> Unit) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) = holder.bind(data[position], viewListener, installListener)

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder = ModuleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_module, parent, false))

    class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(module: ModuleModel, viewListener: (ModuleModel?, View) -> Unit, installListener: (ModuleModel?, View) -> Unit) = with(itemView) {
            tvTitle.text = module.title
            btnInstall.text = if (module.is_installed) context.getString(R.string.uninstall_btn_text) else context.getString(R.string.install_btn_text)
            tvTitle.setOnClickListener { viewListener(module, this) }
            logo.setOnClickListener { viewListener(module, this) }
            btnInstall.setOnClickListener { installListener(module, this) }
        }
    }
}
