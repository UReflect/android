package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import io.ureflect.app.R
import io.ureflect.app.models.CreditCardModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.services.isExpired
import io.ureflect.app.utils.errorSnackbar
import io.ureflect.app.utils.reLogin
import kotlinx.android.synthetic.main.activity_new_credit_card.*


fun Context.newCreditCardIntent(): Intent = Intent(this, NewCreditCard::class.java)

class NewCreditCard : AppCompatActivity() {
    companion object {
        const val TAG = "NewProfileActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var creditCard: CreditCardModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_credit_card)
        queue = Volley.newRequestQueue(this)

        setupUI()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        val color = ResourcesCompat.getColor(resources, R.color.colorText, null)
        cardForm.cardRequired(true)
                .apply { cardEditText.setTextColor(color) }
                .expirationRequired(true)
                .apply { expirationDateEditText.setTextColor(color) }
                .cvvRequired(true)
                .apply { cvvEditText.setTextColor(color) }
                .actionLabel("Create")
                .apply {
                    setOnCardFormSubmitListener {
                        onSubmit()
                    }
                }
                .apply {
                    setOnCardFormValidListener { valid ->
                        btnCreate.isEnabled = valid
                        if (valid) {
                            btnCreate.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
                        } else {
                            btnCreate.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorDisabled, null))
                        }
                    }
                }
                .setup(this)
        cardForm.setCardNumberIcon(R.drawable.ic_credit_card)
        btnCreate.setOnClickListener {
            onSubmit()
        }
    }

    private fun onSubmit() {
        if (cardForm.isValid) {
            generateToken { token ->
                createCreditCard(token) {
                    finish()
                }
            }
        } else {
            cardForm.validate()
        }
    }

    private fun generateToken(callback: (String) -> Unit) {
        val card = Card(cardForm.cardNumber, cardForm.expirationMonth.toInt(), cardForm.expirationYear.toInt(), cardForm.cvv)
        if (card.validateCard()) {
            val stripe = Stripe(this, "pk_test_RUkL2vJmjMptuFlOCokvGGVF")
            loading.visibility = View.VISIBLE
            stripe.createToken(
                    card,
                    object : TokenCallback {
                        override fun onSuccess(token: Token) {
                            loading.visibility = View.GONE
                            callback(token.id)
                        }

                        override fun onError(error: Exception) {
                            loading.visibility = View.GONE
                            errorSnackbar(root, error.localizedMessage)
                        }
                    }
            )
        } else {
            errorSnackbar(root, getString(R.string.invalid_card_error))
        }
    }

    private fun createCreditCard(token: String, callback: () -> Unit) {
        loading.visibility = View.VISIBLE
        queue.add(Api.Misc.createPayment(
                application,
                JsonObject().apply { addProperty("token", token) },
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let {
                        creditCard = it
                        callback()
                    } ?: run {
                        errorSnackbar(root, getString(R.string.api_parse_error))
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.isExpired()) {
                        reLogin(loading, root, queue) {
                            createCreditCard(token, callback)
                        }
                    } else {
                        errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)))
                    }
                }
        ).apply { tag = TAG })
    }
}
