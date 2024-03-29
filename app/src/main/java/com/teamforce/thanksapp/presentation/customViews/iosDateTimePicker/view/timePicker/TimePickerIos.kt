package com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.timePicker


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.Nullable
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.factory.TimeFactory
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.factory.TimeFactoryListener
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.WheelView
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.WheelView.OnWheelViewListener



class TimePickerIos @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), TimeFactoryListener {
    private var emptyView1: WheelView? = null
    private var emptyView2: WheelView? = null
    private var context: Context? = null
    private var container: LinearLayout? = null
    private var offset = 3
    private var textSize = 19
    private var darkModeEnabled = true
    private var isNightTheme = false
    private var hourView: WheelView? = null
    private var minuteView: WheelView? = null
    private val formatView: WheelView? = null
    private var factory: TimeFactory? = null

    init {
        setAttributes(context, attrs)
        init(context)
    }

    @SuppressLint("NonConstantResourceId")
    private fun setAttributes(context: Context, @Nullable attrs: AttributeSet?) {
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.IosDateTimePicker)
        val N = a.indexCount
        for (i in 0 until N) {
            val attr = a.getIndex(i)
            if (attr == R.styleable.IosDateTimePicker_offset) {
                offset = Math.min(a.getInteger(attr, 3), MAX_OFFSET)
            } else if (attr == R.styleable.IosDateTimePicker_darkModeEnabled) {
                darkModeEnabled = a.getBoolean(attr, true)
            } else if (attr == R.styleable.IosDateTimePicker_textSize) {
                textSize = Math.min(a.getInt(attr, MAX_TEXT_SIZE), MAX_TEXT_SIZE)
            }
        }
        a.recycle()
    }

    private fun init(context: Context) {
        this.context = context
        this.orientation = HORIZONTAL
        factory = TimeFactory(this)
        container = LinearLayout(context)
        // TODO LP
        val layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        container!!.layoutParams = layoutParams
        container!!.orientation = HORIZONTAL
        this.addView(container)
        setUpInitialViews()
    }

    private fun setUpInitialViews() {
        context?.let { context ->
            if (darkModeEnabled) {
                checkDarkMode()
            } else {
                isNightTheme = false
            }
            container!!.removeAllViews()
            container!!.addView(createEmptyView1(context))
            container!!.addView(createHourView(context))
            container!!.addView(createMinuteView(context))
            container!!.addView(createEmptyView2(context))
            setupEmptyViews()
        }
    }

    private fun checkDarkMode() {
        val nightModeFlags = getContext().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> isNightTheme = true
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> isNightTheme =
                false
        }
    }

    private fun createMinuteView(context: Context): View {
        minuteView = WheelView(context)
        // TODO LP
        val lp =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        minuteView!!.layoutParams = lp
        minuteView!!.setOnWheelViewListener(object : OnWheelViewListener {
            override fun onSelected(selectedIndex: Int, item: String?) {
                factory!!.setMinute(
                    selectedIndex
                )
            }
        })
        val minutes: MutableList<String?> = ArrayList()
        for (i in 0..59) {
            var str = ""
            if (i < 10) str += "0"
            str += "" + i
            minutes.add(str)
        }
        minuteView!!.isNightTheme = isNightTheme
        minuteView!!.setOffset(offset)
        minuteView!!.setTextSize(textSize.toFloat())
        minuteView!!.setAlignment(View.TEXT_ALIGNMENT_CENTER)
        minuteView!!.setGravity(Gravity.CENTER)
        minuteView!!.setItems(minutes)
        minuteView!!.setSelection(factory!!.getMinute())
        val ly = wheelContainerView(1.0f)
        ly.addView(minuteView)
        return ly
    }

    private fun createHourView(context: Context): View {
        hourView = WheelView(context)
        // TODO LP
        val lp =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        hourView!!.layoutParams = lp
        hourView!!.setOnWheelViewListener(object : OnWheelViewListener {
            override fun onSelected(selectedIndex: Int, item: String?) {
                factory!!.setHour(
                    selectedIndex
                )
            }
        })
        val hours: MutableList<String?> = ArrayList()
        for (i in 0..23) {
            var str = ""
            if (i < 10) str += "0"
            str += "" + i
            hours.add(str)
        }
        hourView!!.isNightTheme = isNightTheme
        hourView!!.setOffset(offset)
        hourView!!.setTextSize(textSize.toFloat())
        hourView!!.setAlignment(View.TEXT_ALIGNMENT_CENTER)
        hourView!!.setGravity(Gravity.CENTER)
        hourView!!.setItems(hours)
        hourView!!.setSelection(factory!!.getHour())
        val ly = wheelContainerView(1.0f)
        ly.addView(hourView)
        return ly
    }

    private fun createEmptyView1(context: Context): LinearLayout {
        emptyView1 = createEmptyWheel(context)
        val ly = wheelContainerView(2.0f)
        ly.addView(emptyView1)
        return ly
    }

    private fun createEmptyView2(context: Context): LinearLayout {
        emptyView2 = createEmptyWheel(context)
        val ly = wheelContainerView(2.0f)
        ly.addView(emptyView2)
        return ly
    }

    private fun setupEmptyViews() {
        val array: MutableList<String?> = ArrayList()
        for (i in 0..29) {
            array.add("")
        }
        emptyView1!!.setTextSize(textSize.toFloat())
        emptyView2!!.setTextSize(textSize.toFloat())
        emptyView1!!.setOffset(offset)
        emptyView2!!.setOffset(offset)
        emptyView1!!.setItems(array)
        emptyView2!!.setItems(array)
    }

    private fun createEmptyWheel(context: Context): WheelView {
        val view = WheelView(context)
        val lp =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.layoutParams = lp
        return view
    }

    private fun wheelContainerView(weight: Float): LinearLayout {
        val layout = LinearLayout(context)
        val layoutParams = LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight)
        layout.layoutParams = layoutParams
        layout.orientation = VERTICAL
        return layout
    }

    override fun onHourChanged(hour: Int) {
        if (hourView!!.getSelectedIndex() != hour) hourView!!.setSelection(hour)
        notifyTimeSelect()
    }

    override fun onMinuteChanged(minute: Int) {
        if (minuteView!!.getSelectedIndex() != minute) minuteView!!.setSelection(minute)
        notifyTimeSelect()
    }

    interface TimeSelectListener {
        fun onTimeSelected(hour: Int, minute: Int)
    }

    private var timeSelectListener: TimeSelectListener? = null
    fun setTimeSelectListener(dataSelectListener: TimeSelectListener?) {
        timeSelectListener = dataSelectListener
    }

    private fun notifyTimeSelect() {
        if (timeSelectListener != null) timeSelectListener!!.onTimeSelected(
            factory!!.getHour(),
            factory!!.getMinute()
        )
    }

    fun getOffset(): Int {
        return offset
    }

    fun setOffset(offset: Int) {
        this.offset = offset
        setUpInitialViews()
    }

    fun setTextSize(textSize: Int) {
        this.textSize = Math.min(textSize, MAX_TEXT_SIZE)
        setUpInitialViews()
    }

    var hour: Int
        get() = factory!!.getHour()
        set(hour) {
            factory!!.setHour(hour)
        }
    var minute: Int
        get() = factory!!.getMinute()
        set(minute) {
            factory!!.setMinute(minute)
        }

    fun setTime(hour: Int, minute: Int) {
        factory!!.setHour(hour)
        factory!!.setMinute(minute)
    }

    companion object {
        private const val MAX_TEXT_SIZE = 20
        private const val MAX_OFFSET = 3
    }
}