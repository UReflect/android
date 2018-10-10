package io.ureflect.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.ureflect.app.activities.loginIntent
import io.ureflect.app.activities.registerIntent
import io.ureflect.app.services.Api
import io.ureflect.app.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Api.log("hello world")

        btnRegister.setOnClickListener {
            startActivity(registerIntent())
            finish()
        }

        btnLogin.setOnClickListener {
            startActivity(loginIntent())
            finish()
        }
    }
}
