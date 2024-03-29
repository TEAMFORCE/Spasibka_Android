package com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.datePicker

import com.teamforce.thanksapp.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.Nullable
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.factory.DateFactoryListener
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.factory.DatePickerFactory
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.WheelView
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.WheelView.OnWheelViewListener




class DatePickerIos @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), DateFactoryListener {
    private var context: Context? = null
    private var container: LinearLayout? = null
    private var offset = 3
    private var factory: DatePickerFactory? = null
    private var dayView: WheelView? = null
    private var monthView: WheelView? = null
    private var yearView: WheelView? = null
    private var emptyView1: WheelView? = null
    private var emptyView2: WheelView? = null
    private var textSize = 19
    private var pickerMode = 0
    private var darkModeEnabled = true
    private var isNightTheme = false

    init {
        init(context)
        setAttributes(context, attrs)
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
                textSize = Math.min(
                    a.getInt(attr, MAX_TEXT_SIZE),
                    MAX_TEXT_SIZE
                )
            } else if (attr == R.styleable.IosDateTimePicker_pickerMode) {
                pickerMode = a.getInt(attr, 0)
            }
        }
        a.recycle()
    }

    private fun init(context: Context) {
        this.context = context
        this.orientation = HORIZONTAL
        factory = DatePickerFactory(this, context)
        container = LinearLayout(context)
        val layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        container!!.layoutParams = layoutParams
        container!!.orientation = HORIZONTAL
        this.addView(container)
        setUpInitialViews()
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

    private fun setUpInitialViews() {
        context?.let { context ->
            container!!.removeAllViews()
            container!!.addView(createEmptyView1(context))
            if (pickerMode == MONTH_ON_FIRST) {
                container!!.addView(createMonthView(context))
                container!!.addView(createDayView(context))
            } else {
                container!!.addView(createDayView(context))
                container!!.addView(createMonthView(context))
            }
            container!!.addView(createYearView(context))
            container!!.addView(createEmptyView2(context))
            setUpCalendar()
        }

    }

    private fun setUpCalendar() {
        Log.i("Calendar", "setUp = " + factory!!.getSelectedDate().toString())
        if (darkModeEnabled) {
            checkDarkMode()
        } else {
            isNightTheme = false
        }
        setUpYearView()
        setUpMonthView()
        setUpDayView()
        setupEmptyViews()
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

    private fun setUpYearView() {
        val date = factory!!.getSelectedDate()
        val years: List<String?> = factory!!.yearList
        yearView!!.isNightTheme = isNightTheme
        yearView!!.setOffset(offset)
        yearView!!.setTextSize(textSize.toFloat())
        yearView!!.setAlignment(View.TEXT_ALIGNMENT_CENTER)
        yearView!!.setGravity(Gravity.END)
        yearView!!.setItems(years)
        yearView!!.setSelection(years.indexOf("" + date.year))
    }

    private fun setUpMonthView() {
        val months: List<String?> = factory!!.monthList
        val date = factory!!.getSelectedDate()
        monthView!!.isNightTheme = isNightTheme
        monthView!!.setTextSize(textSize.toFloat())
        monthView!!.setGravity(Gravity.CENTER)
        monthView!!.setAlignment(View.TEXT_ALIGNMENT_TEXT_START)
        monthView!!.setOffset(offset)
        monthView!!.setItems(months)
        monthView!!.setSelection(date.month - factory!!.monthMin)
    }

    private fun setUpDayView() {
        val date = factory!!.getSelectedDate()
        val days: List<String?> = factory!!.dayList
        dayView!!.isNightTheme = isNightTheme
        dayView!!.setOffset(offset)
        dayView!!.setTextSize(textSize.toFloat())
        dayView!!.setGravity(Gravity.END)
        dayView!!.setAlignment(if (pickerMode == DAY_ON_FIRST) View.TEXT_ALIGNMENT_CENTER else View.TEXT_ALIGNMENT_TEXT_END)
        dayView!!.setOffset(offset)
        dayView!!.setItems(days)
        dayView!!.setSelection(date.day - 1) //Day start from 1
    }

    private fun createYearView(context: Context): LinearLayout {
        yearView = WheelView(context)
        val lp =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        yearView!!.layoutParams = lp
        yearView!!.setOnWheelViewListener(object : OnWheelViewListener {
            override fun onSelected(selectedIndex: Int, item: String?) {
                factory!!.setSelectedYear(
                    item?.toInt() ?: 0
                )
            }
        })
        val ly = wheelContainerView(3.0f)
        ly.addView(yearView)
        return ly
    }

    private fun createMonthView(context: Context): LinearLayout {
        monthView = WheelView(context)
        val lp =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        monthView!!.layoutParams = lp
        monthView!!.setOnWheelViewListener(object : OnWheelViewListener {
            override fun onSelected(selectedIndex: Int, item: String?) {
                factory!!.setSelectedMonth(
                    factory!!.monthMin + selectedIndex
                )
            }
        })
        val ly = wheelContainerView(3.0f)
        ly.addView(monthView)
        return ly
    }

    private fun createDayView(context: Context): LinearLayout {
        dayView = WheelView(context)
        val lp =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dayView!!.layoutParams = lp
        dayView!!.setOnWheelViewListener(object : OnWheelViewListener {
            override fun onSelected(selectedIndex: Int, item: String?) {
                factory!!.setSelectedDay(
                    selectedIndex + 1
                )
            }
        })
        val ly = wheelContainerView(2.0f)
        ly.addView(dayView)
        return ly
    }

    private fun createEmptyView1(context: Context): LinearLayout {
        emptyView1 = createEmptyWheel(context)
        val ly = wheelContainerView(1.5f)
        ly.addView(emptyView1)
        return ly
    }

    private fun createEmptyView2(context: Context): LinearLayout {
        emptyView2 = createEmptyWheel(context)
        val ly = wheelContainerView(1.0f)
        ly.addView(emptyView2)
        return ly
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
    /**
     * @return minDate
     */
    /**
     * Implement current min date
     *
     * @param date
     */
    var minDate: Long
        get() = factory!!.getMinDate().getDate()
        set(date) {
            factory!!.setMinDate(date)
        }
    /**
     * @return maxDate
     */
    /**
     * Implement current max date
     *
     * @param date
     */
    var maxDate: Long
        get() = factory!!.getMaxDate().getDate()
        set(date) {
            factory!!.setMaxDate(date)
        }
    /**
     * @return date
     */
    /**
     * Implement current selected date
     *
     * @param date
     */
    var date: Long
        get() = factory!!.getSelectedDate().getDate()
        set(date) {
            factory!!.setSelectedDate(date)
        }

    fun getOffset(): Int {
        return offset
    }

    fun setOffset(offset: Int) {
        this.offset = offset
        setUpCalendar()
    }

    fun setTextSize(textSize: Int) {
        this.textSize = Math.min(textSize, MAX_TEXT_SIZE)
        setUpCalendar()
    }

    fun setPickerMode(pickerMode: Int) {
        this.pickerMode = pickerMode
        setUpInitialViews()
    }

    override fun onYearChanged() {
        setUpMonthView()
        setUpDayView()
        notifyDateSelect()
    }

    override fun onMonthChanged() {
        setUpDayView()
        notifyDateSelect()
    }

    override fun onDayChanged() {
        notifyDateSelect()
    }

    override fun onConfigsChanged() {
        setUpCalendar()
    }

    /**
     * @return
     */
    fun isDarkModeEnabled(): Boolean {
        return darkModeEnabled
    }

    /**
     * @param darkModeEnabled
     */
    fun setDarkModeEnabled(darkModeEnabled: Boolean) {
        this.darkModeEnabled = darkModeEnabled
        setUpCalendar()
    }

    interface DataSelectListener {
        fun onDateSelected(date: Long, day: Int, month: Int, year: Int)
    }

    private var dataSelectListener: DataSelectListener? = null
    fun setDataSelectListener(dataSelectListener: DataSelectListener?) {
        this.dataSelectListener = dataSelectListener
    }

    private fun notifyDateSelect() {
        val date = factory!!.getSelectedDate()
        dataSelectListener?.onDateSelected(
            date.getDate(),
            date.day,
            date.month,
            date.year
        )
    }

    companion object {
        const val MONTH_ON_FIRST = 0
        const val DAY_ON_FIRST = 1
        private const val MAX_TEXT_SIZE = 20
        private const val MAX_OFFSET = 3
    }
}