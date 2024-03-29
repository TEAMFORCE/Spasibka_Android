package com.teamforce.thanksapp.presentation.customViews.NonSwipeableViewPager

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonSwipeableViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Запретить перехватывать события касания
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Запретить обработку событий касания
        return false
    }
}