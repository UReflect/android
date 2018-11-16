package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.adapters.ListFragmentPagerAdapter
import io.ureflect.app.fragments.SignUpCredentialsFragment
import io.ureflect.app.fragments.SignUpIdentityFragment
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
import io.ureflect.app.utils.TOKEN
import io.ureflect.app.utils.toStorage
import kotlinx.android.synthetic.main.activity_signup.*

fun Context.registerIntent(): Intent = Intent(this, SignUp::class.java)

class SignUp : AppCompatActivity() {
    companion object {
        const val TAG = "SignUpActivity"
    }

    private lateinit var queue: RequestQueue
    private lateinit var adapter: ListFragmentPagerAdapter
    private var position = Steps.IDENTITY.step
    private val fragments = ArrayList<Fragment>()
    private var firstName = ""
    private var lastName = ""
    private var email = ""
    private var password = ""

    enum class Steps(val step: Int) {
        IDENTITY(0),
        CREDENTIAL(1),
        SIGN_UP(2)
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
                SignUpIdentityFragment({
                    next(Steps.CREDENTIAL)
                }, { firstName: String ->
                    this.firstName = firstName
                }, { lastName: String ->
                    this.lastName = lastName
                })
        )
        fragments.add(
                SignUpCredentialsFragment({
                    next(Steps.SIGN_UP)
                }, { email: String ->
                    this.email = email
                }, { password: String ->
                    this.password = password
                })
        )
        adapter = ListFragmentPagerAdapter(supportFragmentManager, fragments)
        viewPager.adapter = adapter
        viewPager.currentItem = position
    }

    private fun next(step: Steps) {
        position = step.step
        when (position) {
            Steps.SIGN_UP.step -> signUp()
            else -> Handler().postDelayed({ viewPager.currentItem = position }, 100)
        }
    }

    private fun signUp() {
        queue.add(Api.Auth.signup(
                JsonObject().apply { addProperty("email", email) }
                        .apply { addProperty("password", password) }
                        .apply { addProperty("name", "$firstName $lastName") },
                Response.Listener { response ->
                    val user = response.data?.user?.toStorage(this.application)
                    val token = response.data?.token?.toStorage(this.application, TOKEN)
                    if (user == null || token == null) {
                        Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                        return@Listener
                    }
                    toHomeView()
                },
                Response.ErrorListener { error ->
                    position = fragments.size
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
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
