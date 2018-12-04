package io.ureflect.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.ureflect.app.activities.homeIntent
import io.ureflect.app.activities.loginIntent
import io.ureflect.app.activities.registerIntent
import io.ureflect.app.services.Api
import io.ureflect.app.utils.TOKEN
import io.ureflect.app.utils.fromStorage
import kotlinx.android.synthetic.main.activity_main.*

fun Context.mainIntent(): Intent {
    val intent = Intent(this, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    return intent
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Api.log("hello world")

        btnRegister.transformationMethod = null
        btnRegister.setOnClickListener {
            startActivity(registerIntent())
        }

        btnLogin.transformationMethod = null
        btnLogin.setOnClickListener {
            startActivity(loginIntent())
        }

        redirect()
    }

    private fun redirect() {
        if (fromStorage<String>(application, TOKEN) != null) {
            startActivity(homeIntent())
        }
    }
}
