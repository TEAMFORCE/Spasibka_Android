package com.teamforce.thanksapp.presentation.customViews.bottomSheetBehavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


fun <T : Any> KClass<T>.getDeclaredFieldOrNull(fieldName: String): Field? {
    return try {
        val field = java.getDeclaredField(fieldName)
        field.isAccessible = true
        field
    } catch (e: NoSuchFieldException) {
        null
    }
}


class DeclaredField<T: Any?> (name: String) {
    private val field = BottomSheetBehavior::class.getDeclaredFieldOrNull(name)

    operator fun <V: View> getValue(bottomSheetBehavior: BottomSheetBehavior<V>, property: KProperty<*>): T? {
        @Suppress("UNCHECKED_CAST")
        return field?.get(bottomSheetBehavior) as? T
    }

    operator fun <V: View> setValue(bottomSheetBehavior: BottomSheetBehavior<V>, property: KProperty<*>, value: T) {
        field?.set(bottomSheetBehavior, value)
    }
}

/**
 * BottomSheetBehavior supports multiple child nested scrolls.
 */
class BottomSheetBehavior : BottomSheetBehavior<View> {
    constructor() : super()
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val nestedScrollingChildRefs = mutableListOf<WeakReference<View>>()

    private var nestedScrollingChildRef by DeclaredField<WeakReference<View?>?>("nestedScrollingChildRef")

    private fun findScrollingChildren(view: View) {
        if (view.isVisible.not()) {
            return
        }

        if (ViewCompat.isNestedScrollingEnabled(view)) {
            nestedScrollingChildRefs.add(WeakReference(view))
        }

        if (view is ViewGroup) {
            view.children.forEach {
                findScrollingChildren(it)
            }
        }
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        return super.onLayoutChild(parent, child, layoutDirection).also {
            if (nestedScrollingChildRefs.isEmpty()) {
                findScrollingChildren(child)
            }
        }
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: View,
        event: MotionEvent
    ): Boolean {
        nestedScrollingChildRefs
            .mapNotNull { it.get() }
            .find { parent.isPointInChildBounds(it, event.x.toInt(), event.y.toInt()) }
            .also { nestedScrollingChildRef = WeakReference(it) }

        return super.onInterceptTouchEvent(parent, child, event)
    }
}