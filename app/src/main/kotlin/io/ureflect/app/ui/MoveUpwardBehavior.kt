package io.ureflect.app.ui

import android.content.Context
import android.support.annotation.Keep
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.view.View

@Keep
class MoveUpwardBehavior : CoordinatorLayout.Behavior<View> {

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean = dependency is Snackbar.SnackbarLayout

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val translationY = -Math.min(0.0f, dependency.translationY - dependency.height)
        (child.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = translationY.toInt()
        if (child is NestedScrollView && translationY.toInt() == dependency.height) {
            child.smoothScrollBy(0, dependency.height)
        }
        child.requestLayout()
        return true
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        (child.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = 0
        if (child is NestedScrollView) {
            child.smoothScrollBy(0, -dependency.height)
        }
        child.requestLayout()
        super.onDependentViewRemoved(parent, child, dependency)
    }
}