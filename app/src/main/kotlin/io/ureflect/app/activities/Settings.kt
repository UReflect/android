package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.adapters.CreditCardAdapter
import io.ureflect.app.models.CreditCardModel
import io.ureflect.app.models.UserModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.services.expired
import io.ureflect.app.utils.*
import kotlinx.android.synthetic.main.activity_settings.*

fun Context.settingsIntent(): Intent = Intent(this, Settings::class.java)

class Settings : AppCompatActivity() {
    companion object {
        const val TAG = "SettingsActivity"
    }

    private var triedOnce = false
    private lateinit var queue: RequestQueue
    private lateinit var user: UserModel
    private lateinit var creditCards: List<CreditCardModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        Api.log("starting Settings activity")
        queue = Volley.newRequestQueue(this)
        this.user = UserModel.fromStorage(application)
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        loadCreditCards()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun payloadError(): Boolean {
        var error = false
        error = error || !evEmailLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_email_required))
        error = error || !evEmailLayout.validate({ s -> s.isValidEmail() }, getString(R.string.form_error_email_incorrect))
        error = error || !evPasswordLayout.validate({ s -> s.isNotEmpty() }, getString(R.string.form_error_password_required))
        return error
    }

    private fun payloadAutoValidate() {
        triedOnce = true
        evEmailLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_email_required))
        evEmailLayout.autoValidate({ s -> s.isValidEmail() }, getString(R.string.form_error_email_incorrect))
        evPasswordLayout.autoValidate({ s -> s.isNotEmpty() }, getString(R.string.form_error_password_required))
    }


    private fun setupUI() {
        evEmail.setText(user.email)
        evPassword.setText(user.password)

        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        rvCreditCards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCreditCards.addItemDecoration(EqualSpacingItemDecoration(px, EqualSpacingItemDecoration.HORIZONTAL))

        btnRetry.setOnClickListener {
            loadCreditCards()
        }

        btn.setOnClickListener {
            if (!payloadError()) {
                update()
            } else if (!triedOnce) {
                payloadAutoValidate()
            }
        }
    }

    private fun loadCreditCards() {
        loading.visibility = View.VISIBLE
        btnRetry.visibility = View.GONE
        queue.add(Api.Misc.payments(
                application,
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    this.creditCards = response.data ?: ArrayList()
                    rvCreditCards.adapter = CreditCardAdapter(creditCards, {
                        startActivity(newCreditCardIntent())
                    }, {
                        //TODO : something
                    })
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    btnRetry.visibility = View.VISIBLE
                    errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)), error.expired())
                }
        ).apply { tag = Home.TAG })
    }

    private fun update() {
        loading.visibility = View.VISIBLE
        queue.add(Api.User.update(
                application,
                user.ID,
                JsonObject().apply { addProperty("email", evEmail.text.toString()) }
                        .apply { addProperty("password", evPassword.text.toString()) },
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.let {
                        user = it.apply { password = evPassword.text.toString() }.toStorage(application)
                        successSnackbar(root)
                    } ?: run {
                        errorSnackbar(root, getString(R.string.api_parse_error))
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    errorSnackbar(root, error.errMsg(this, getString(R.string.api_parse_error)), error.expired())
                }
        ).apply { tag = NewMirror.TAG })
    }
}
