package io.ureflect.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import io.ureflect.app.R
import io.ureflect.app.adapters.MirrorAdapter
import io.ureflect.app.mainIntent
import io.ureflect.app.models.ApiErrorResponse
import io.ureflect.app.models.Mirror
import io.ureflect.app.models.User
import io.ureflect.app.services.Api
import io.ureflect.app.utils.Storage
import kotlinx.android.synthetic.main.activity_home.*
import java.text.SimpleDateFormat
import java.util.*

fun Context.homeIntent(): Intent {
    val intent = Intent(this, Home::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    return intent
}

class Home : AppCompatActivity() {
    private val TAG = "HomeActivity"
    private lateinit var queue: RequestQueue
    private lateinit var mirrors: ArrayList<Mirror>
    private lateinit var mirrorAdapter: MirrorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Api.log("starting home activity")
        queue = Volley.newRequestQueue(this)
        setupUI()
        loadMirrors()
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll(TAG)
    }

    private fun setupUI() {
        val formatter = SimpleDateFormat("EEEE dd MMMM", Locale.FRANCE)
        tvDate.text = formatter.format(Date()).toUpperCase()
        val user = User.fromStorage(this.application)
        tvTitle.text = getString(R.string.home_title_text, user.name)
        rvMirrors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        btnLogout.setOnClickListener { _ ->
            logout()
        }
    }

    private fun loadMirrors() {
        queue.add(Api.mirrors(
                this.application,
                Response.Listener { response ->
                    mirrors = response.data!!
                    mirrorAdapter = MirrorAdapter(mirrors, {
                        //TODO : create Mirror
                        Log.e(TAG, "Create Mirror")
                    }, { mirror ->
                        //TODO : Mirror details
                        Log.e(TAG, "Select Mirror : " + mirror.name)
                    })
                    rvMirrors.adapter = mirrorAdapter
                },
                Response.ErrorListener { error ->
                    val errorResponse = Gson().fromJson(String(error.networkResponse.data), ApiErrorResponse::class.java)
                    Toast.makeText(this, errorResponse.error, Toast.LENGTH_LONG).show()
                }
        ))
    }

    private fun logout() {
        Storage.clear(this.application)
        startActivity(mainIntent())
        finish()
    }
}
