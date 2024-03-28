package com.teamforce.thanksapp.presentation.adapter.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class HorizontalDecoratorForLastItem(
    private val amountRightPadding: Int = 50
): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)


        // Для последнего элемента устанавливаем отдельный отступ
        if(parent.getChildLayoutPosition(view) == parent.adapter?.itemCount?.minus(1)){
            outRect.right = amountRightPadding
        }
    }


}