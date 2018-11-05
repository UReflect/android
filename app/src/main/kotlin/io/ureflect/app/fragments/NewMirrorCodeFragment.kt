package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import io.ureflect.app.R
import io.ureflect.app.services.Api
import io.ureflect.app.services.errMsg
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
        v.civCode.addOnCompleteListener {
            v.civCode.setEditable(true)
        }
        v.tvGo.setOnClickListener {
            if (!credentialsPayloadError(v)) {
                v.civCode.clearError()
                activity?.application?.let { app ->
                    join(v, app)
                }
            } else {
                v.civCode.error = getString(R.string.form_error_code_too_short)
            }
        }
    }

    private fun join(v: View, app: Application) {
        val data = JsonObject()
        data.addProperty("join_code", v.civCode.code)
        v.loading.visibility = View.VISIBLE
        queue.add(Api.Mirror.join(
                app,
                data,
                Response.Listener { response ->
                    v.loading.visibility = View.GONE
                    val mirrorId = response.data?.ID
                    if (mirrorId == null) {
                        Snackbar.make(v.root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                        hideKeyboard()
                        return@Listener
                    }
                    setMirrorId(mirrorId)
                    next(0)
                },
                Response.ErrorListener { error ->
                    v.loading.visibility = View.GONE
                    hideKeyboard()
                    Snackbar.make(v.root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                }
        ))
    }

    private fun hideKeyboard() {
        var view = activity?.currentFocus
        if (view == null) {
            view = View(activity)
        }
        activity?.getSystemService(Activity.INPUT_METHOD_SERVICE).let {
            (it as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}