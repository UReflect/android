package io.ureflect.app.utils

import android.app.Activity
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import io.ureflect.app.R
import io.ureflect.app.mainIntent
import java.io.Serializable

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) = beginTransaction().func().commit()

fun Snackbar.setTextColor(color: Int): Snackbar = apply { view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(color) }

fun Snackbar.setBackgroundColor(color: Int): Snackbar = apply { view.setBackgroundColor(color) }

fun errorSnackbar(root: View, msg: String) = Snackbar.make(root, msg, Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}.show()

fun AppCompatActivity.hideKeyboard() {
    var view = currentFocus
    if (view == null) {
        view = View(this)
    }
    getSystemService(Activity.INPUT_METHOD_SERVICE).let {
        (it as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun AppCompatActivity.errorSnackbar(root: View, msg: String, expired: Boolean = false) = Snackbar.make(root, msg, Snackbar.LENGTH_INDEFINITE)
        .apply { if (expired) setAction("Logout") { logout() } else setAction("Dismiss") {} }
        .show()

fun Context.successSnackbar(root: View) = Snackbar.make(root, getString(R.string.success_text), Snackbar.LENGTH_SHORT)
        .setAction("Dismiss") {}
        .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorSuccess, null))
        .setActionTextColor(ResourcesCompat.getColor(resources, R.color.colorText, null))
        .show()

fun AppCompatActivity.logout() {
    Storage.clear(application)
    startActivity(mainIntent())
    finish()
}

fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int) = supportFragmentManager.inTransaction {
    setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
    add(frameId, fragment)
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) = supportFragmentManager.inTransaction {
    setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
    replace(frameId, fragment)
}

fun AppCompatActivity.removeFragment(fragment: Fragment) = supportFragmentManager.inTransaction {
    setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
    remove(fragment)
}

inline fun <reified T : Serializable> Activity.getArg(identifier: String): T? {
    val args = intent.extras
    args?.getSerializable(identifier)?.let { arg ->
        if (arg is T) {
            return arg
        }
    }
    return null
}

fun String.isValidEmail(): Boolean = isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun TextInputLayout.afterTextChanged(afterTextChanged: (String) -> Unit) = editText?.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        afterTextChanged.invoke(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
})

/**
 * @returns true if valid, false if not valid
 */
fun TextInputLayout.validate(validator: (String) -> Boolean, message: String): Boolean {
    val valid = validator(editText?.text.toString())
    error = if (valid) null else message
    return valid
}

fun TextInputLayout.autoValidate(validator: (String) -> Boolean, message: String) = afterTextChanged {
    error = if (validator(it)) null else message
}