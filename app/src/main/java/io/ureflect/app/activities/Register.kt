package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.ureflect.app.R
import io.ureflect.app.services.Api

fun Context.registerIntent(): Intent {
    return Intent(this, Register::class.java)
}

class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Api.log("hello world")
    }
}
