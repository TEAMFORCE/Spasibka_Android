package com.teamforce.thanksapp.presentation.customViews.DatePicker

import android.app.DatePickerDialog
import android.content.Context
import android.view.ContextThemeWrapper
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.google.android.material.datepicker.DayViewDecorator
import com.google.android.material.datepicker.MaterialDatePicker
import com.teamforce.thanksapp.R
import java.text.SimpleDateFormat
import java.util.*

class BirthDayPicker(
    private val context: Context,
    private val fragmentManager: FragmentManager
): DatePickerDialog.OnDateSetListener{


   // private var datePickerDialog: DatePickerDialog? = null
   private val picker = MaterialDatePicker.Builder.datePicker().setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
       .setTheme(R.style.DatePicker)
       .setTextInputFormat(
       SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    ).build()
    private var _dateWithoutYear = MutableLiveData<String>()
    val dateWithoutYear = _dateWithoutYear
    private var _dateWithYear = MutableLiveData<String>()
    val dateWithYear = _dateWithYear
    private var _dateForServer = MutableLiveData<String>()
    val dateForServer = _dateForServer

    init {
        initDatePicker()
    }

    fun showDatePicker(){
     //   datePickerDialog?.show()
        picker.show(fragmentManager, picker.toString())
    }


    private fun initDatePicker() {
//        val dateSetListener =
//            DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
//                _dateWithoutYear.value = makeDateString(day, month + 1)
//                _dateForServer.value = makeDateForSendToServer(day.addLeadZero(), (month + 1).addLeadZero(), year)
//                _dateWithYear.value = makeDateStringWithYear(day, month + 1, year)
//
//
//            }
//        val cal = Calendar.getInstance()
//        val year = cal[Calendar.YEAR]
//        val month = cal[Calendar.MONTH]
//        val day = cal[Calendar.DAY_OF_MONTH]
//        datePickerDialog =
//            DatePickerDialog(context, dateSetListener, year, month, day)
//        datePickerDialog!!.datePicker.maxDate = Calendar.getInstance().timeInMillis - 86400000L

        picker.addOnPositiveButtonClickListener { selectedDateMillis ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDateMillis

            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH] + 1 // Месяц начинается с 0
            val day = calendar[Calendar.DAY_OF_MONTH]

            val dateWithoutYear = makeDateString(day, month)
            val dateForServer = makeDateForSendToServer(day.addLeadZero(), month.addLeadZero(), year)
            val dateWithYear = makeDateStringWithYear(day, month, year)

            _dateWithoutYear.value = dateWithoutYear
            _dateForServer.value = dateForServer
            _dateWithYear.value = dateWithYear
        }


    }

    private fun makeDateStringWithYear(day: Int, month: Int, year: Int): String {
        return day.toString() + " " +  getMonthFormat(month, context) + " " +  year
    }

    private fun makeDateString(day: Int, month: Int): String {
        return day.toString() + " " +  getMonthFormat(month, context)
    }

    private fun makeDateForSendToServer(day: String, month: String, year: Int): String {
        return "$year-$month-$day"
    }

    private fun getMonthFormat(month: Int, context: Context): String {
        if (month == 1) return context.getString(R.string.januaryDate)
        if (month == 2) return context.getString(R.string.februaryDate)
        if (month == 3) return context.getString(R.string.marchDate)
        if (month == 4) return context.getString(R.string.aprilDate)
        if (month == 5) return context.getString(R.string.mayDate)
        if (month == 6) return context.getString(R.string.juneDate)
        if (month == 7) return context.getString(R.string.julyDate)
        if (month == 8) return context.getString(R.string.augustDate)
        if (month == 9) return context.getString(R.string.septemberDate)
        if (month == 10) return context.getString(R.string.octoberDate)
        if (month == 11) return context.getString(R.string.novemberDate)
        return if (month == 12) context.getString(R.string.decemberDate) else context.getString(R.string.januaryDate)

    }

    private fun Int.addLeadZero(): String {
        if(this in 1..9) return "0$this"

        return this.toString()
    }

    override fun onDateSet(
        view: android.widget.DatePicker?,
        year: Int,
        month: Int,
        dayOfMonth: Int
    ) {
        TODO("Not yet implemented")
    }
}