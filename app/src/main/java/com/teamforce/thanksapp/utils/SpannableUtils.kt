package com.teamforce.thanksapp.utils

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View

class SpannableUtils {

    companion object {
        fun createClickableSpannable(
            string: String,
            color: String?,
            onClick: (() -> Unit)?
        ): SpannableString {
            val spannableString = SpannableString(string)

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(p0: View) {
                    onClick?.invoke()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = Color.parseColor(color)
                }
            }
            spannableString.setSpan(
                clickableSpan,
                0,
                string.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableString

        }

        fun createSpannable(
            string: String,
            color: String?,
        ): SpannableString {
            val spannableString = SpannableString(string)

            spannableString.setSpan(
                ForegroundColorSpan(Color.parseColor(color)),
                0,
                string.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableString

        }
    }
}