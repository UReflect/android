package io.ureflect.app.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.models.CreditCardModel
import kotlinx.android.synthetic.main.view_creditcard.view.*

class CreditCardAdapter(val data: List<CreditCardModel>, val addListener: (CreditCardModel?) -> Unit, val selectListener: (CreditCardModel?) -> Unit) : RecyclerView.Adapter<CreditCardAdapter.CreditCardViewHolder>() {

    override fun onBindViewHolder(holder: CreditCardViewHolder, position: Int) = when (position) {
        0 -> holder.bind(null, addListener)
        else -> holder.bind(data[position - 1], selectListener)
    }

    override fun getItemCount(): Int = data.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditCardViewHolder {
        return when (viewType) {
            EntityAdapter.TYPE_ADD_ENTITY -> AddViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_add, parent, false))
            else -> CreditCardAdapterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_creditcard, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> EntityAdapter.TYPE_ADD_ENTITY
        else -> EntityAdapter.TYPE_ENTITY
    }

    abstract inner class CreditCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(creditCard: CreditCardModel?, listener: (CreditCardModel?) -> Unit)
    }

    inner class AddViewHolder(itemView: View) : CreditCardViewHolder(itemView) {
        override fun bind(creditCard: CreditCardModel?, listener: (CreditCardModel?) -> Unit) = with(itemView) {
            setOnClickListener { listener(creditCard) }
        }
    }

    inner class CreditCardAdapterViewHolder(itemView: View) : CreditCardViewHolder(itemView) {
        override fun bind(creditCard: CreditCardModel?, listener: (CreditCardModel?) -> Unit) = with(itemView) {
            when (creditCard?.brand) {
                "Visa" -> {
                    tvName.text = context.getString(R.string.card_visa_text, creditCard.last4)
                    ivLogo.setImageResource(io.ureflect.app.R.drawable.icon_visa)
                }
                "MasterCard" -> {
                    tvName.text = context.getString(R.string.card_mastercard_text, creditCard.last4)
                    ivLogo.setImageResource(io.ureflect.app.R.drawable.icon_mc)
                }
                "American Express" -> {
                    tvName.text = context.getString(R.string.card_amex_text, creditCard.last4)
                    ivLogo.setImageResource(io.ureflect.app.R.drawable.icon_amex)
                }
                "Discover" -> {
                    tvName.text = context.getString(R.string.card_discover_text, creditCard.last4)
                    ivLogo.setImageResource(io.ureflect.app.R.drawable.icon_discover)
                }
                "JCB" -> {
                    tvName.text = context.getString(R.string.card_jcb_text, creditCard.last4)
                    ivLogo.setImageResource(io.ureflect.app.R.drawable.icon_jcb)
                }
                "Diners Club" -> {
                    tvName.text = context.getString(R.string.card_diners_text, creditCard.last4)
                    ivLogo.setImageResource(io.ureflect.app.R.drawable.icon_diners)
                }
                else -> {
                    tvName.text = context.getString(R.string.card_other_text, creditCard?.last4)
                    ivLogo.setImageResource(android.R.color.transparent)
                }
            }

            ivCheck.visibility = if (creditCard?.isClicked == true) View.VISIBLE else View.GONE
        }
    }
}
