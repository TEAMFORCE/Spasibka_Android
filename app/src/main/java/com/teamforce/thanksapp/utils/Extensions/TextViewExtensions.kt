package com.teamforce.thanksapp.utils.Extensions

import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.widget.TextView
import androidx.annotation.ColorInt
import com.teamforce.thanksapp.utils.isNullOrEmptyMy


fun TextView.paintLinks(@ColorInt colorInt: Int) {
    val spannable = SpannableString(text)
    for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
        spannable.setSpan(object : URLSpan(u.url) {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = colorInt
                ds.isUnderlineText = false
                ds.isFakeBoldText = true
            }
        }, spannable.getSpanStart(u), spannable.getSpanEnd(u), 0)
    }
    text = spannable
}

fun TextView.enableOnClickableLinks(){
    this.apply {
        linksClickable = true
        movementMethod = LinkMovementMethod.getInstance()
        autoLinkMask = Linkify.WEB_URLS
        setTextIsSelectable(true)
    }
}

fun TextView.setNameOrTg(name: String?, surname: String?, tgName: String?){
    if(name.isNullOrEmptyMy() && surname.isNullOrEmptyMy()){
        this.text = tgName
    }else{
        this.text = "$name $surname"
    }
}

fun TextView.addCurrencyWord(count: Int, cases: List<String>): String{
    val cases = listOf("form1", "form5", "form4") // Здесь нужно заменить на получение форм склонений с бекенда

    val index = when (count % 10) {
        1 -> if (count % 100 != 11) 0 else 2
        in 2..4 -> if (count % 100 !in 12..14) 1 else 2
        else -> 2
    }

    return cases[index]

}