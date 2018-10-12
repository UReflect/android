package io.ureflect.app.utils

import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_signin.*

fun String.isValidEmail(): Boolean = this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun TextInputLayout.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.editText?.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

/**
 * @returns true if valid, false if not valid
 */
fun TextInputLayout.validate(validator: (String) -> Boolean, message: String): Boolean {
    val valid = validator(this.editText?.text.toString())
    this.error = if (valid) null else message
    return valid
}

fun TextInputLayout.autoValidate(validator: (String) -> Boolean, message: String) {
    this.afterTextChanged {
        this.error = if (validator(it)) null else message
    }
}