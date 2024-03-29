package com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.popup

import android.content.Context

import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.model.DateModel
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.datePicker.DatePickerIos


class DatePickerIosPopup(
    context: Context,
    picker: DatePickerIos
) : PickerIosPopup(context) {

    private var listener: OnDateSelectListener? = null

    init {
        init(picker)
    }

    private fun init(picker: DatePickerIos) {
        setCancelable(false)
        setCanceledOnTouchOutside(true)
        confirm?.setOnClickListener { view ->
            if (listener != null) {
                val dateModel = DateModel(picker.date)
                listener!!.onDateSelected(
                    picker,
                    dateModel.getDate(),
                    dateModel.day,
                    dateModel.month + 1,
                    dateModel.year
                )
            }
            dismiss()
        }
        addView(picker)
    }

    fun setListener(listener: OnDateSelectListener?) {
        this.listener = listener
    }

    class Builder {
        private var context: Context? = null
        private var datePicker: DatePickerIos? = null
        private var listener: OnDateSelectListener? = null

        fun from(context: Context): Builder {
            this.context = context
            datePicker = DatePickerIos(context)
            return this
        }

        fun textSize(textSize: Int): Builder {
            datePicker?.setTextSize(textSize)
            return this
        }

        fun startDate(startDate: Long): Builder {
            datePicker!!.minDate = startDate
            return this
        }

        fun endDate(endDate: Long): Builder {
            datePicker!!.maxDate = endDate
            return this
        }

        fun currentDate(currentDate: Long): Builder {
            datePicker?.date = (currentDate)
            return this
        }

        fun offset(offset: Int): Builder {
            datePicker?.setOffset(offset)
            return this
        }

        fun darkModeEnabled(darkModeEnabled: Boolean): Builder {
            datePicker?.setDarkModeEnabled(darkModeEnabled)
            return this
        }

        fun pickerMode(appearanceMode: Int): Builder {
            datePicker?.setPickerMode(appearanceMode)
            return this
        }

        fun listener(listener: OnDateSelectListener?): Builder {
            this.listener = listener
            return this
        }

        fun build(): DatePickerIosPopup {
            val popup = DatePickerIosPopup(context!!, datePicker!!)
            popup.setListener(listener)
            return popup
        }

//        fun build(theme: Int): DatePickerIosPopup {
//            val popup = DatePickerIosPopup(context, theme, datePicker)
//            popup.setListener(listener)
//            return popup
//        }
    }

    interface OnDateSelectListener {
        fun onDateSelected(dp: DatePickerIos, date: Long, day: Int, month: Int, year: Int)
    }
}