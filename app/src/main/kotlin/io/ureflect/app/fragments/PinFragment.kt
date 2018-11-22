package io.ureflect.app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ureflect.app.R
import kotlinx.android.synthetic.main.fragment_pin.*
import kotlinx.android.synthetic.main.view_pin_number.view.*

@SuppressLint("ValidFragment")
class PinFragment(var next: (String) -> Unit) : CoordinatorRootFragment() {
    private val TAG = "PinFragment"
    private lateinit var list: List<View>
    private var pinVal = ""
    var isSetup = false

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
        delete.setOnClickListener {
            pinVal = pinVal.dropLast(1)
            updatePin()
        }
        tvTitle.visibility = if (isSetup) View.VISIBLE else View.GONE
    }

    private fun updatePin() {
        tvPin.text = if (isSetup) pinVal else "*".repeat(pinVal.length)
        if (tvPin.text.length == 4) {
            next(pinVal)
        }
    }
}
