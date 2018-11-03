package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.models.Responses.ApiErrorResponse
import io.ureflect.app.services.Api
import kotlinx.android.synthetic.main.fragment_new_mirror_code.view.*

@SuppressLint("ValidFragment")
class NewMirrorCodeFragment(var next: (Int) -> Unit, var setMirrorId: (String) -> Unit) : Fragment() {
    private lateinit var queue: RequestQueue

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_new_mirror_code, container, false)

        queue = Volley.newRequestQueue(this.context)
        setupUI(rootView)

        return rootView
    }

    private fun credentialsPayloadError(v: View): Boolean {
        return v.civCode.code.length != 4
    }

    private fun setupUI(v: View) {
        v.tvGo.setOnClickListener {
            if (!credentialsPayloadError(v)) {
                val data = JsonObject()
                data.addProperty("join_code", v.civCode.code)

                val app = activity?.application
                if (app != null) {
                    queue.add(Api.join(
                            app,
                            data,
                            Response.Listener { response ->
                                val mirrorId = response.data?.ID
                                if (mirrorId == null) {
                                    Snackbar.make(v.root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                                    return@Listener
                                }
                                setMirrorId(mirrorId)
                                next(1)
                            },
                            Response.ErrorListener { error ->
                                val errorResponse = Gson().fromJson(String(error.networkResponse.data), ApiErrorResponse::class.java)
                                errorResponse.error?.let { msg -> Snackbar.make(v.root, msg, Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show() }
                            }
                    ))
                }
            }
        }
    }
}