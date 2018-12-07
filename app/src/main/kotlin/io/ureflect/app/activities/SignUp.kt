package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.adapters.ListFragmentPagerAdapter
import io.ureflect.app.fragments.CoordinatorRootFragment
import io.ureflect.app.fragments.SignUpCredentialsFragment
import io.ureflect.app.fragments.SignUpIdentityFragment
import io.ureflect.app.models.UserModel
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.utils.TOKEN
import io.ureflect.app.utils.errorSnackbar
import io.ureflect.app.utils.toStorage
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signup.*

fun Context.registerIntent(): Intent = Intent(this, SignUp::class.java)

class SignUp : AppCompatActivity() {
    companion object {
        const val TAG = "SignUpActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var adapter: ListFragmentPagerAdapter
    private var position = Steps.IDENTITY.step
    private val fragments = ArrayList<CoordinatorRootFragment>()
    private var firstName = ""
    private var lastName = ""
    private var email = ""
    private var password = ""

    enum class Steps(val step: Int) {
        IDENTITY(0),
        CREDENTIAL(1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        queue = Volley.newRequestQueue(this)
        setupFragments()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupFragments() {
        fragments.add(
                SignUpIdentityFragment { firstName: String, lastName: String ->
                    this.firstName = firstName
                    this.lastName = lastName
                    next(Steps.CREDENTIAL)
                }
        )
        fragments.add(
                SignUpCredentialsFragment { email: String, password: String ->
                    this.email = email
                    this.password = password
                    signUp()
                }
        )
        adapter = ListFragmentPagerAdapter(supportFragmentManager, fragments)
        viewPager.adapter = adapter
        viewPager.currentItem = position
    }

    private fun next(step: Steps) {
        position = step.step
        Handler().postDelayed({ viewPager.currentItem = position }, 100)
    }

    private fun signUp() {
        val root = fragments[Steps.CREDENTIAL.step].getRoot()
        val loader = fragments[Steps.CREDENTIAL.step].getLoader()
        loader.visibility = View.VISIBLE
        queue.add(Api.Auth.signup(
                JsonObject().apply { addProperty("email", email.toLowerCase()) }
                        .apply { addProperty("password", password) }
                        .apply { addProperty("name", "$firstName $lastName") },
                Response.Listener { response ->
                    loader.visibility = View.INVISIBLE
                    val user = response.data?.user?.apply { password = evPassword.text.toString() }?.toStorage(application, UserModel.TAG)
                    val token = response.data?.token?.toStorage(application, TOKEN)
                    if (user == null || token == null) {
                        errorSnackbar(root, getString(R.string.api_parse_error))
                        return@Listener
                    }
                    toHomeView()
                },
                Response.ErrorListener { error ->
                    loader.visibility = View.INVISIBLE
                    var errmsg = error.errMsg(this, getString(R.string.api_parse_error))
                    if (errmsg.contains("idx_email")) {
                        errmsg = getString(R.string.email_taken_error)
                    }
                    errorSnackbar(root, errmsg)
                }
        ).apply { tag = TAG })
    }

    private fun toHomeView() {
        startActivity(homeIntent())
        finish()
    }

    override fun onBackPressed() {
        when (position) {
            Steps.IDENTITY.step -> super.onBackPressed()
            else -> {
                position -= 1
                viewPager.currentItem = position
            }
        }
    }
}
