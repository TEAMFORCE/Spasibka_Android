package com.teamforce.thanksapp.presentation.customViews.TextView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.presentation.theme.Themable

class ShortenNumberTextView : AppCompatTextView, Themable {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setText(text: CharSequence?, type: BufferType?) {
        val intValue = text.toString().toIntOrNull()
        if(intValue == null) super.setText(text, type)
        else{
            super.setText(shortenNumber(intValue), type)
        }
    }

    private fun shortenNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> {
                val millions = number / 1_000_000
                val tenths = Math.round(number % 1_000_000 / 100_000f)
                "$millions.$tenths M"
            }
            number >= 1_000 -> {
                val thousands = number / 1_000
                val tenths = Math.round(number % 1_000 / 100f)
                "$thousands.$tenths K"
            }
            number >= 99 -> {
                "${number}+"
            }
            else -> number.toString()
        }
    }

    override fun setThemeColor(theme: ColorsModel) {
        setTextColor(ColorStateList.valueOf(Color.parseColor(theme.mainBrandColor)))
    }
}

interface ShortenedNumber{
    private fun setShortenNumber(number: Int): String{
        return when {
            number >= 1_000_000 -> {
                val millions = number / 1_000_000
                val tenths = Math.round(number % 1_000_000 / 100_000f)
                "$millions.$tenths M"
            }
            number >= 1_000 -> {
                val thousands = number / 1_000
                val tenths = Math.round(number % 1_000 / 100f)
                "$thousands.$tenths K"
            }
            number >= 99 -> {
                "${number}+"
            }
            else -> number.toString()
        }
    }

}

