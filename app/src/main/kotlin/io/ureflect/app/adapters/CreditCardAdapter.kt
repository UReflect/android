package io.ureflect.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.models.CreditCardModel
import kotlinx.android.synthetic.main.view_credit_card.view.*

class CreditCardAdapter(data: List<CreditCardModel>, addListener: (CreditCardModel?, View) -> Unit, selectListener: (CreditCardModel?, View) -> Unit) : EntityAdapter<CreditCardModel>(data, addListener, selectListener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityAdapterViewHolder = when (viewType) {
        EntityAdapter.TYPE_ADD_ENTITY -> AddViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_add, parent, false).apply { resize(parent, this) })
        else -> CreditCardAdapterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_credit_card, parent, false).apply { resize(parent, this) })
    }

    inner class CreditCardAdapterViewHolder(itemView: View) : EntityAdapterViewHolder(itemView) {
        override fun bind(entity: CreditCardModel?, listener: (CreditCardModel?, View) -> Unit) = with(itemView) {
            setOnLongClickListener {
                entity?.let { card ->
                    listener(card, this)
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }
            when (entity?.brand?.toLowerCase()) {
                "visa" -> {
                    tvName.text = context.getString(R.string.card_visa_text, entity.last4)
                    ivLogo.setImageResource(R.drawable.bt_ic_visa)
                }
                "mastercard" -> {
                    tvName.text = context.getString(R.string.card_mastercard_text, entity.last4)
                    ivLogo.setImageResource(R.drawable.bt_ic_mastercard)
                }
                "american express" -> {
                    tvName.text = context.getString(R.string.card_amex_text, entity.last4)
                    ivLogo.setImageResource(R.drawable.bt_ic_amex)
                }
                "discover" -> {
                    tvName.text = context.getString(R.string.card_discover_text, entity.last4)
                    ivLogo.setImageResource(R.drawable.bt_ic_discover)
                }
                "jcb" -> {
                    tvName.text = context.getString(R.string.card_jcb_text, entity.last4)
                    ivLogo.setImageResource(R.drawable.bt_ic_jcb)
                }
                "diners club" -> {
                    tvName.text = context.getString(R.string.card_diners_text, entity.last4)
                    ivLogo.setImageResource(R.drawable.bt_ic_diners_club)
                }
                "union pay" -> {
                    tvName.text = context.getString(R.string.card_diners_text, entity.last4)
                    ivLogo.setImageResource(R.drawable.bt_ic_diners_club)
                }
                else -> {
                    tvName.text = context.getString(R.string.card_other_text, entity?.last4)
                    ivLogo.setImageResource(R.drawable.bt_ic_unknown)
                }
            }
        }
    }
}