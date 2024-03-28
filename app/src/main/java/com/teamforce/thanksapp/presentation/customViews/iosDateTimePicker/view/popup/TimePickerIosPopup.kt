package com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.popup

import android.content.Context
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.timePicker.TimePickerIos


class TimePickerIosPopup(
    context: Context,
    picker: TimePickerIos
) : PickerIosPopup(context) {
    private var listener: OnTimeSelectListener? = null

   init {
       init(picker)
   }

    private fun init(timePicker: TimePickerIos) {
        setCancelable(false)
        setCanceledOnTouchOutside(true)
        confirm?.setOnClickListener { view ->
            if (listener != null) listener!!.onTimeSelected(
                timePicker,
                timePicker.hour,
                timePicker.minute
            )
            dismiss()
        }
        addView(timePicker)
    }

    fun setListener(listener: OnTimeSelectListener?) {
        this.listener = listener
    }

    class Builder {
        private var context: Context? = null
        private var timePicker: TimePickerIos? = null
        private var listener: OnTimeSelectListener? = null

        fun from(context: Context): Builder {
            this.context = context
            timePicker = TimePickerIos(context)
            return this
        }

        fun textSize(textSize: Int): Builder {
            timePicker?.setTextSize(textSize)
            return this
        }

        fun offset(offset: Int): Builder {
            timePicker?.setOffset(offset)
            return this
        }

        fun setTime(hour: Int, minute: Int): Builder {
            timePicker?.setTime(hour, minute)
            return this
        }

        fun listener(listener: OnTimeSelectListener?): Builder {
            this.listener = listener
            return this
        }

        fun build(): TimePickerIosPopup {
            val popup = TimePickerIosPopup(context!!, timePicker!!)
            popup.setListener(listener)
            return popup
        }

//        fun build(theme: Int): TimePickerIosPopup {
//            val popup = TimePickerIosPopup(context, theme, timePicker)
//            popup.setListener(listener)
//            return popup
//        }
    }

    interface OnTimeSelectListener {
        fun onTimeSelected(timePicker: TimePickerIos?, hour: Int, minute: Int)
    }
}