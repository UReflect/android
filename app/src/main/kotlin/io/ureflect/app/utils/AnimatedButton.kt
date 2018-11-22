package io.ureflect.app.utils

import android.content.Context
import android.graphics.LightingColorFilter
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button

class AnimatedButton(context: Context, attrs: AttributeSet) : Button(context, attrs) {
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