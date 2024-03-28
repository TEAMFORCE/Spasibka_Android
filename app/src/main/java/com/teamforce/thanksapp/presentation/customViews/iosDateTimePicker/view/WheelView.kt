package com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics.DENSITY_DEFAULT
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.teamforce.thanksapp.R
import timber.log.Timber


class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {
    var TAG = WheelView::class.java.simpleName
    private var displayItemCount = 0
    private var selectedIndex = 1
    private var offset: Int = OFF_SET_DEFAULT
    private var configChanged = false
    private var initialY = 0
    private var scrollerTask: Runnable? = null
    private val newCheck = 50L
    var isNightTheme = false

    interface OnWheelViewListener {
        fun onSelected(selectedIndex: Int, item: String?)
    }

    init {
        init(context)
    }

    private var context: Context? = null
    private var views: LinearLayout? = null


    private var items: MutableList<String>? = null
    private var textSize = 19f
    private var ALIGNMENT: Int = View.TEXT_ALIGNMENT_CENTER
    private var GRAVITY: Int = Gravity.CENTER

    fun getItems(): List<String>? {
        return items
    }

    fun setItems(list: List<String?>?) {
        if (items == null) {
            items = mutableListOf()
        }
        items?.let { items ->
            items.clear()
            list?.map {
                if(!it.isNullOrEmpty()) items.add(it)
            }
            for (i in 0 until offset) {
                items.add(0, "")
                items.add("")
            }
        }

        initData()
    }

    fun setOffset(offset: Int) {
        if (this.offset != offset) {
            configChanged = true
        }
        this.offset = offset
    }

    private fun init(context: Context) {
        this.context = context
        Log.d(TAG, "parent: " + this.getParent())
        this.setVerticalScrollBarEnabled(false)
        this.setOverScrollMode(View.OVER_SCROLL_NEVER)
        views = LinearLayout(context)
        views?.let { it.orientation = LinearLayout.VERTICAL }
        this.addView(views)
        scrollerTask = Runnable {
            val newY: Int = getScrollY()
            if (initialY - newY == 0) { // stopped
                val remainder = initialY % itemHeight
                val divided = initialY / itemHeight
                if (remainder == 0) {
                    selectedIndex = divided + offset
                    onSelectedCallBack()
                } else {
                    if (remainder > itemHeight / 2) {
                        this@WheelView.post(Runnable {
                            this@WheelView.smoothScrollTo(0, initialY - remainder + itemHeight)
                            selectedIndex = divided + offset + 1
                            onSelectedCallBack()
                        })
                    } else {
                        this@WheelView.post(Runnable {
                            this@WheelView.smoothScrollTo(0, initialY - remainder)
                            selectedIndex = divided + offset
                            onSelectedCallBack()
                        })
                    }
                }
            } else {
                initialY = getScrollY()
                this@WheelView.postDelayed(scrollerTask, newCheck)
            }
        }
    }

    fun startScrollerTask() {
        initialY = getScrollY()
        this.postDelayed(scrollerTask, newCheck)
    }

    private fun initData() {
        views?.removeAllViews()
        displayItemCount = offset * 2 + 1
        for (item in items!!) {
            views?.addView(createView(item))
        }
        val scrollHeight = (selectedIndex - offset) * itemHeight
        refreshItemView(scrollHeight)
    }

    fun setTextSize(textSize: Float) {
        if (this.textSize != textSize) {
            configChanged = true
        }
        this.textSize = textSize
    }

    fun setAlignment(alignment: Int) {
        ALIGNMENT = alignment
    }

    fun setGravity(gravity: Int) {
        GRAVITY = gravity
    }

    var itemHeight = 0
    private fun createView(item: String): LinearLayout {
        val viewContainer = LinearLayout(context)
        val tv = TextView(context)
        tv.isClickable = true
        val textLP =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textLP.gravity = GRAVITY
        tv.layoutParams = textLP
        tv.isSingleLine = true
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        tv.text = " $item"
        tv.textAlignment = ALIGNMENT
        tv.gravity = Gravity.CENTER
        val padding = dip2px(1f).toInt()
        tv.setPadding(padding, padding, padding, padding)
        val containerParam =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getViewMeasuredHeight(tv))
        viewContainer.layoutParams = containerParam
        viewContainer.addView(tv)
        if (0 == itemHeight || configChanged) {
            itemHeight = getViewMeasuredHeight(tv)
            Timber.tag(TAG).d("itemHeight: %s", itemHeight)
            views?.layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                itemHeight * displayItemCount
            )
            val lp: LinearLayout.LayoutParams = this.layoutParams as LinearLayout.LayoutParams
            this.layoutParams = LinearLayout.LayoutParams(lp.width, itemHeight * displayItemCount)
            configChanged = false
        }
        return viewContainer
    }

    override fun onScrollChanged(l: Int, t: Int, oldL: Int, oldT: Int) {
        super.onScrollChanged(l, t, oldL, oldT)
        refreshItemView(t)
        scrollDirection = if (t > oldT) {
            SCROLL_DIRECTION_DOWN
        } else {
            SCROLL_DIRECTION_UP
        }
    }

    private fun refreshItemView(y: Int) {
        var position = y / itemHeight + offset
        val remainder = y % itemHeight
        val divided = y / itemHeight
        if (remainder == 0) {
            position = divided + offset
        } else {
            if (remainder > itemHeight / 2) {
                position = divided + offset + 1
            }
        }
        val childSize: Int = views?.childCount ?: 0
        for (i in 0 until childSize) {
            val itemView: LinearLayout = views?.getChildAt(i) as LinearLayout
            val item: TextView = itemView.getChildAt(0) as TextView
            if (position == i) {
                item.setTextColor(context!!.getColor(R.color.color_text))
                if (item.getTextSize() != textSize) item.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    textSize
                )
                val text: String = item.getText().toString()
                item.setText(text.trim { it <= ' ' })
            } else if (i < position) {
                if (i == position - 1) {
                    item.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize - 2)
                    item.setTextColor(context!!.getColor(R.color.color_grey))
                } else if (i == position - 2) {
                    item.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize - 3)
                    item.setTextColor(context!!.getColor(R.color.color_grey1))
                } else {
                    item.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize - 4)
                    item.setTextColor(context!!.getColor(R.color.color_grey2))
                }
                var text: String = item.getText().toString()
                text = "  " + text.trim { it <= ' ' }
                item.setText(text)
            }
            if (i > position) {
                if (i == position + 1) {
                    item.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize - 2)
                    item.setTextColor(context!!.getColor(R.color.color_grey))
                } else if (i == position + 2) {
                    item.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize - 3)
                    item.setTextColor(context!!.getColor(R.color.color_grey1))
                } else {
                    item.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize - 4)
                    item.setTextColor(context!!.getColor(R.color.color_grey2))
                }
                var text: String = item.text.toString()
                text = "  " + text.trim { it <= ' ' }
                item.setText(text)
            }
        }
    }

    private var selectedAreaBorder: FloatArray? = null

    private fun obtainSelectedAreaBorder(): FloatArray {
        return if (null == selectedAreaBorder) {
            selectedAreaBorder = FloatArray(2)
            selectedAreaBorder!![0] = (itemHeight * offset).toFloat()
            selectedAreaBorder!![1] = (itemHeight * (offset + 1)).toFloat()
            selectedAreaBorder!!
        }else{
            selectedAreaBorder!!
        }
    }

    private var scrollDirection = -1
    private var paint: Paint? = null
    private var viewWidth: Int? = 0

    override fun setBackground(background: Drawable?) {
        if (viewWidth == 0) {
            viewWidth = context?.resources?.displayMetrics?.widthPixels
            Timber.tag(TAG).d("viewWidth: %s", viewWidth)
        }
        if (null == paint) {
            paint = Paint()
            paint!!.color = context!!.getColor(R.color.color_grey2)
            paint!!.strokeWidth = dip2px(1f)
        }
//        val backgroundInner: Drawable?
//        backgroundInner = object : Drawable() {
//            override fun draw(canvas: Canvas) {
//                if(viewWidth != null && paint != null){
//                    canvas.drawLine(
//                        0F,
//                        obtainSelectedAreaBorder()[0], viewWidth!!.toFloat(), obtainSelectedAreaBorder()[0], paint!!
//                    )
//                    canvas.drawLine(
//                        0F,
//                        obtainSelectedAreaBorder()[1], viewWidth!!.toFloat(), obtainSelectedAreaBorder()[1], paint!!
//                    )
//                }
//
//            }
//
//            override fun setAlpha(alpha: Int) {}
//            override fun setColorFilter(cf: ColorFilter?) {}
//
//            override fun getOpacity(): Int {
//                return PixelFormat.UNKNOWN
//            }
//
//
//            @get:SuppressLint("WrongConstant")
//            val opacity: Int
//                get() = 0
//        }
        super.setBackground(background)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "w: $w, h: $h, oldw: $oldw, oldh: $oldh")
        viewWidth = w
        setBackground(null)
    }

    private fun onSelectedCallBack() {
        if (onWheelViewListener != null) {
            if (selectedIndex - offset >= 0) onWheelViewListener!!.onSelected(
                selectedIndex - offset,
                items!![selectedIndex]
            )
        }
    }

    fun setSelection(position: Int) {
        selectedIndex = position + offset
        this.post(Runnable { this@WheelView.smoothScrollTo(0, position * itemHeight) })
    }

    val selectedItem: String
        get() = items!![selectedIndex]

    fun getSelectedIndex(): Int {
        return selectedIndex - offset
    }

    override fun fling(velocityY: Int) {
        super.fling(velocityY / 3)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            startScrollerTask()
        }
        return super.onTouchEvent(ev)
    }

    private var onWheelViewListener: OnWheelViewListener? = null
    fun setOnWheelViewListener(onWheelViewListener: OnWheelViewListener?) {
        this.onWheelViewListener = onWheelViewListener
    }

    private fun dip2px(dpValue: Float): Float {
        val scale: Int = context?.getResources()?.getDisplayMetrics()?.density?.toInt() ?: DENSITY_DEFAULT
        return (dpValue * scale + 0.5f)
    }

    private fun getViewMeasuredHeight(view: View): Int {
        val width: Int = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val expandSpec: Int = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        view.measure(width, expandSpec)
        return view.getMeasuredHeight()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Stop ScrollView from getting involved once you interact with the View
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            val p: ViewParent = parent
            p.requestDisallowInterceptTouchEvent(true)
        }
        return super.onInterceptTouchEvent(ev)
    }

    companion object {
        const val OFF_SET_DEFAULT = 1
        private const val SCROLL_DIRECTION_UP = 0
        private const val SCROLL_DIRECTION_DOWN = 1
    }
}