package com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.utils

import java.util.*


object DateUtils {
    fun getTimeMiles(year: Int, month: Int, day: Int): Long {
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        val maxDayCount: Int = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, Math.min(day, maxDayCount))
        return calendar.getTimeInMillis()
    }

    val currentTime: Long
        get() {
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.getTimeInMillis()
        }

    fun getMonthDayCount(timeStamp: Long): Int {
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(timeStamp)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getDay(timeStamp: Long): Int {
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(timeStamp)
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun getMonth(timeStamp: Long): Int {
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(timeStamp)
        return calendar.get(Calendar.MONTH)
    }

    fun getYear(timeStamp: Long): Int {
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(timeStamp)
        return calendar.get(Calendar.YEAR)
    }

    val currentHour: Int
        get() {
            val calendar: Calendar = Calendar.getInstance()
            return calendar.get(Calendar.HOUR_OF_DAY)
        }
    val currentMinute: Int
        get() {
            val calendar: Calendar = Calendar.getInstance()
            return calendar.get(Calendar.MINUTE)
        }
}