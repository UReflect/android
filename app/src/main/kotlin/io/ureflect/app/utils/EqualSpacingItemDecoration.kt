package io.ureflect.app.utils

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

class EqualSpacingItemDecoration @JvmOverloads constructor(private val spacing: Int, private var displayMode: Int = -1, private val strict: Boolean = false) : RecyclerView.ItemDecoration() {
    private var forceTop = false

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        val position = parent.getChildViewHolder(view).adapterPosition
        val itemCount = state?.itemCount ?: 1
        val layoutManager = parent.layoutManager
        setSpacingForDirection(outRect, layoutManager, position, itemCount)
    }

    fun forceTop(b: Boolean) {
        this.forceTop = b
    }

    private fun setSpacingForDirection(outRect: Rect,
                                       layoutManager: RecyclerView.LayoutManager,
                                       position: Int,
                                       itemCount: Int) {

        // Resolve display mode automatically
        if (displayMode == -1) {
            displayMode = resolveDisplayMode(layoutManager)
        }

        when (displayMode) {
            HORIZONTAL -> {
                outRect.left = if (position == 0 && strict) 0 else spacing
                outRect.right = if (position == itemCount - 1 && !strict) spacing else 0
                outRect.top = if (strict) if (!forceTop) 0 else spacing else spacing
                outRect.bottom = if (strict) 0 else spacing
            }
            VERTICAL -> {
                outRect.left = if (strict) 0 else spacing
                outRect.right = if (strict) 0 else spacing
                outRect.top = if (position == 0 && strict) if (!forceTop) 0 else spacing else spacing
                outRect.bottom = if (position == itemCount - 1 && !strict) spacing else 0
            }
            GRID -> if (layoutManager is GridLayoutManager) {
                val cols = layoutManager.spanCount
                val rows = itemCount / cols

                outRect.left = spacing
                outRect.right = if (position % cols == cols - 1) spacing else 0
                outRect.top = spacing
                outRect.bottom = if (position / cols == rows - 1) spacing else 0
            }
        }
    }

    private fun resolveDisplayMode(layoutManager: RecyclerView.LayoutManager): Int {
        if (layoutManager is GridLayoutManager) return GRID
        return if (layoutManager.canScrollHorizontally()) HORIZONTAL else VERTICAL
    }

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val GRID = 2
    }
}