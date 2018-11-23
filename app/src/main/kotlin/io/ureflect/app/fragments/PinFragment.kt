package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import io.ureflect.app.fragments.BackPressedFragment.Companion.HANDLED
import io.ureflect.app.fragments.BackPressedFragment.Companion.NOT_HANDLED
import io.ureflect.app.utils.errorSnackbar
import kotlinx.android.synthetic.main.fragment_pin.*
import kotlinx.android.synthetic.main.view_pin_number.view.*

@SuppressLint("ValidFragment")
class PinFragment(var next: (String) -> Unit) : CoordinatorRootFragment(), BackPressedFragment {
    private lateinit var list: List<View>
    private var pinVal = ""
    private var confirmPinVal = ""
    var isDoublePass = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_pin, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        list = listOf<View>(number0, number1, number2, number3, number4, number5, number6, number7, number8, number9)
        var i = 0
        list.forEach {
            it.tvNumber.text = i.toString()
            it.setOnClickListener { view ->
                if (pinVal.length < 4) {
                    pinVal += view.tvNumber.text
                    updatePin()
                }
            }
            i++
        }
        empty.tvNumber.text = ""

        setTitleFirstPass()

        delete.setOnClickListener {
            pinVal = pinVal.dropLast(1)
            updatePin()
        }
    }

    private fun setTitleFirstPass() {
        tvTitle.text = if (isDoublePass) getString(R.string.new_profile_pin_title_text) else getString(R.string.new_profile_pin_title_check_text)
    }

    private fun setTitleSecondPass() {
        tvTitle.text = getString(R.string.new_profile_confirm_pin_title_text)
    }

    private fun match(): Boolean = confirmPinVal == pinVal

    private fun secondPass(): Boolean = confirmPinVal != ""

    private fun pinFull(): Boolean = pinVal.length == 4

    private fun updatePin() {
        tvPin.text = "*".repeat(pinVal.length)
        if (pinFull()) {
            if (!isDoublePass) {
                next(pinVal)
                return
            }
            if (secondPass()) {
                if (match()) {
                    next(pinVal)
                    return
                }
                confirmPinVal = ""
                setTitleFirstPass()
                errorSnackbar(getRoot(), getString(R.string.new_profile_pin_match_error))
            } else {
                confirmPinVal = pinVal
                setTitleSecondPass()
            }
            tvPin.text = ""
            pinVal = ""
        }
    }

    override fun backPressed(): Boolean {
        if (isDoublePass && secondPass()) {
            confirmPinVal = ""
            pinVal = ""
            setTitleFirstPass()
            updatePin()
            return HANDLED
        }
        return NOT_HANDLED
    }
}
