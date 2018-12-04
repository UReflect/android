package io.ureflect.app.ui

import android.content.Context
import android.graphics.LightingColorFilter
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import android.view.MotionEvent

class AnimatedButton(context: Context, attrs: AttributeSet) : AppCompatButton(context, attrs) {
    init {
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.background.colorFilter = LightingColorFilter(-0x777778, 0x000000).apply { invalidate() }
                MotionEvent.ACTION_UP -> v.background.clearColorFilter().apply { invalidate() }
            }
            false
        }
    }
}