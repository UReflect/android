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
import kotlinx.android.synthetic.main.fragment_new_mirror_code.*

@SuppressLint("ValidFragment")
class NewMirrorCodeFragment(var next: () -> Unit, var setMirrorId: (String) -> Unit) : Fragment() {
    private lateinit var queue: RequestQueue

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_new_mirror_code, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        queue = Volley.newRequestQueue(this.context)
        setupUI()
    }

    private fun credentialsPayloadError(): Boolean {
        return civCode.code.length != 4
    }

    private fun setupUI() {
        civCode.addOnCompleteListener {
            civCode.setEditable(true)
        }
        tvGo.setOnClickListener {
            if (!credentialsPayloadError()) {
                civCode.clearError()
                activity?.application?.let { app ->
                    join(app)
                }
            } else {
                civCode.error = getString(R.string.form_error_code_too_short)
            }
        }
    }

    private fun join(app: Application) {
        val data = JsonObject()
        data.addProperty("join_code", civCode.code)
        loading.visibility = View.VISIBLE
        queue.add(Api.Mirror.join(
                app,
                data,
                Response.Listener { response ->
                    loading.visibility = View.GONE
                    response.data?.ID?.let { mirrorId ->
                        setMirrorId(mirrorId)
                        next()
                    } ?: run {
                        Snackbar.make(root, getString(R.string.api_parse_error), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
                        hideKeyboard()
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    hideKeyboard()
                    Snackbar.make(root, error.errMsg(getString(R.string.api_parse_error)), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()
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