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
    private val TAG = "SignUpActivity"
    private val IDENTITY = 0
    private val CREDENTIAL = 1
    private val SIGN_UP = 2
    private lateinit var queue: RequestQueue
    private lateinit var adapter: ListFragmentPagerAdapter
    private var position = 0
    private val fragments = ArrayList<Fragment>()
    private var firstName = ""
    private var lastName = ""
    private var email = ""
    private var password = ""

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
        fragments.add(SignUpIdentityFragment({
            next(CREDENTIAL)
        }, { firstName: String ->
            this.firstName = firstName
        }, { lastName: String ->
            this.lastName = lastName
        }))
        fragments.add(SignUpCredentialsFragment({
            next(SIGN_UP)
        }, { email: String ->
            this.email = email
        }, { password: String ->
            this.password = password
        }))
        adapter = ListFragmentPagerAdapter(supportFragmentManager, fragments)
        viewPager.adapter = adapter
        viewPager.currentItem = position
    }

    private fun next(i: Int) {
        position = i
        when (position) {
            SIGN_UP -> signUp()
            else -> Handler().postDelayed({ viewPager.currentItem = position }, 100)
        }
    }

    private fun signUp() {
        val data = JsonObject()
        data.addProperty("email", email)
        data.addProperty("password", password)
        data.addProperty("name", "$firstName $lastName")

        queue.add(Api.Auth.signup(
                data,
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
        ))
    }

    private fun toHomeView() {
        startActivity(homeIntent())
        finish()
    }

    override fun onBackPressed() {
        when (position) {
            IDENTITY -> super.onBackPressed()
            else -> {
                position -= 1
                viewPager.currentItem = position
            }
        }
    }
}
