package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.adapters.ListFragmentPagerAdapter
import io.ureflect.app.fragments.SignUpCredentialsFragment
import io.ureflect.app.fragments.SignUpIdentityFragment
import io.ureflect.app.models.Responses.ApiErrorResponse
import io.ureflect.app.services.Api
import io.ureflect.app.utils.TOKEN
import io.ureflect.app.utils.toStorage
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.fragment_signup_credentials.*

fun Context.registerIntent(): Intent {
    return Intent(this, SignUp::class.java)
}

class SignUp : AppCompatActivity() {
    private val TAG = "SignUpActivity"
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
        fragments.add(SignUpIdentityFragment({ i: Int ->
            next(i)
        }, { firstName: String ->
            this.firstName = firstName
        }, { lastName: String ->
            this.lastName = lastName
        }))
        fragments.add(SignUpCredentialsFragment({ i: Int ->
            next(i)
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
        position = i + 1
        if (position < fragments.size) {
            Handler().postDelayed({ viewPager.currentItem = i + 1 }, 100)
        } else if (position == fragments.size) {
            signUp()
        }
    }

    private fun signUp() {
        val data = JsonObject()
        data.addProperty("email", email)
        data.addProperty("password", password)
        data.addProperty("name", "$firstName $lastName")

        queue.add(Api.signup(
                data,
                Response.Listener { response ->
                    val user = response.data?.user?.toStorage(this.application)
                    val token = response.data?.token?.toStorage(this.application, TOKEN)
                    if (user == null || token == null) {
                        tvError.text = getString(R.string.api_parse_error)
                        return@Listener
                    }
                    toHomeView()
                },
                Response.ErrorListener { error ->
                    val errorResponse = Gson().fromJson(String(error.networkResponse.data), ApiErrorResponse::class.java)
                    tvError.text = errorResponse.error
                    position = fragments.size
                }
        ))
    }

    private fun toHomeView() {
        startActivity(homeIntent())
        finish()
    }

    override fun onBackPressed() {
        if (position == 0) {
            super.onBackPressed()
        } else {
            position -= 1
            viewPager.currentItem = position
        }
    }
}
