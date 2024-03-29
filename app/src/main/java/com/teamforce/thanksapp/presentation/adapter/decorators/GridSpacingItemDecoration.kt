package com.teamforce.thanksapp.presentation.adapter.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) :
    ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount
        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount // левый отступ
            outRect.right = (column + 1) * spacing / spanCount // правый отступ
            if (position < spanCount) {
                outRect.top = spacing // верхний отступ для первого ряда
            }
            outRect.bottom = spacing // нижний отступ
        } else {
            outRect.left = column * spacing / spanCount // левый отступ
            outRect.right = spacing - (column + 1) * spacing / spanCount // правый отступ
            if (position >= spanCount) {
                outRect.top = spacing // верхний отступ для всех элементов, кроме первого ряда
            }
        }
    }
}
