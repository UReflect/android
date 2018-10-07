package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.ureflect.app.services.Api
import kotlinx.android.synthetic.main.activity_login.*

fun Context.loginIntent(): Intent = Intent(this, Login::class.java)

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Api.log("starting login activity")

        setupUI()
    }

    private fun setupUI() {
        btnForgotPassword.setOnClickListener {}
        btnLogin.setOnClickListener {
            
        }
    }
}
