package com.teamforce.thanksapp.utils


import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import androidx.annotation.AnimRes
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.google.android.material.imageview.ShapeableImageView
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.FastBlur.doBlur
import com.teamforce.thanksapp.utils.branding.Branding
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.ceil

fun BlurView.blur(radius: Float, context: Context, rootView: ViewGroup){

    this.outlineProvider = ViewOutlineProvider.BACKGROUND
    this.clipToOutline = true


    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
        this.setupWith(rootView, RenderScriptBlur(context))
            .setBlurRadius(radius)
    }else{
        this.setupWith(rootView, RenderEffectBlur())
            .setBlurRadius(radius)
    }
}

fun View.rotateAnim(fromDegrees: Float, toDegrees: Float, duration: Long = 300) {
    val rotate = RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    rotate.duration = duration
    rotate.fillAfter = true
    this.startAnimation(rotate)
}

fun View.fastBlur(bitmap: Bitmap?){
    if(bitmap != null){
        val scaleFactor = 8f
        val radius = 2f

        var overlay = Bitmap.createBitmap(
            (this.measuredWidth / scaleFactor).toInt(),
            (this.measuredHeight / scaleFactor).toInt(), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(overlay!!)
        canvas.translate(-this.left / scaleFactor, -this.top / scaleFactor)
        canvas.scale(1 / scaleFactor, 1 / scaleFactor)
        val paint = Paint()
        paint.flags = Paint.FILTER_BITMAP_FLAG
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // TODO Radius must be more than 1
        overlay = doBlur(overlay, radius.toInt(), true)!!
        this.background = BitmapDrawable(resources, overlay)
    }else{
        Log.e("ViewExt", "Bitmap null")
    }

}

fun View.animateScale(scale: Float, duration: Long = 200) {
    val scaleXAnimator = ObjectAnimator.ofFloat(this, View.SCALE_X, scale)
    val scaleYAnimator = ObjectAnimator.ofFloat(this, View.SCALE_Y, scale)

    scaleXAnimator.duration = duration
    scaleYAnimator.duration = duration

    scaleXAnimator.start()
    scaleYAnimator.start()
}

fun View.animateElevation(targetElevation: Float, duration: Long = 200) {
    this.animate()
        .setDuration(duration)
        .translationZ(targetElevation)
        .start()
}

fun View.traverseViewsTheming() {
    val viewsStack = Stack<View>()
    viewsStack.push(this)

    while (!viewsStack.isEmpty()) {
        val view = viewsStack.pop()
        if (view is Themable) view.setThemeColor(Branding.appTheme)

        if (view is ViewGroup) {
            val childCount = view.childCount
            for (i in childCount - 1 downTo 0) {
                val childView = view.getChildAt(i)
                viewsStack.push(childView)
            }
        }
    }
}
fun <T> concatenate(vararg lists: List<T>?): List<T> {
    val result: MutableList<T> = ArrayList()
    for (list in lists) {
        list?.let {
            result.addAll(list)
        }
    }
    return result
}


val Int.dp: Int
    get() = ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)).toInt()

val Float.dp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)


fun View.visible() = this.apply {
    if(this.visibility != View.VISIBLE){
        alpha = 0f
        visibility = View.VISIBLE

        animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(null)
    }
}

fun View.invisible() = this.apply {
    if(this.visibility == View.VISIBLE) {
        alpha = 1f
        visibility = View.GONE

        animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(null)
    }
}

fun View.changeVisibility() = this.apply {
    if (this.isVisible) visibility = View.GONE
    else visibility = View.VISIBLE
}

fun View.invisibileWithCustomAnim(@AnimRes anim: Int){
    val animation = AnimationUtils.loadAnimation(this.context, anim)
    this.startAnimation(animation)
    this.visibility = View.GONE
}

fun View.visibileWithCustomAnim(@AnimRes anim: Int){
    val animation = AnimationUtils.loadAnimation(this.context, anim)
    this.startAnimation(animation)
    this.visibility = View.VISIBLE
}

// Пример использования
//    private fun setupStickyFooter() {
//        binding.scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
//            binding.challengeOrganizerSticky.isVisible =
//                !(v as NestedScrollView).isViewVisible(binding.challengeOrganizer)
//        }
//    }

fun NestedScrollView.isViewVisible(fixedView: View): Boolean {
    val scrollBounds = Rect()
    this.getDrawingRect(scrollBounds)
    val top = fixedView.y
    val bottom = fixedView.height + top
    return scrollBounds.bottom > bottom
}

fun View.delayBlock(
    durationInMillis: Long,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: ()-> Unit
) : Job? = findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
    lifecycleOwner.lifecycle.coroutineScope.launch(dispatcher) {
        delay(durationInMillis)
        block()
    }
}