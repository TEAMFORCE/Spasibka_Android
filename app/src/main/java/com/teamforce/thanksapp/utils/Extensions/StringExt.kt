package com.teamforce.thanksapp.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.URLSpan
import android.util.Patterns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.utils.branding.Branding
import java.util.Locale


fun getMimeType(url: String?): String? {
    var type: String? = null
    val extension = MimeTypeMap.getFileExtensionFromUrl(url)
    if (extension != null) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return type
}

fun CharSequence.removeLinksUnderline(): Spanned {
    val spannable = SpannableString(this)
    for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
        spannable.setSpan(object : URLSpan(u.url) {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                Branding.brand?.colorsJson?.mainBrandColor?.let { ds.color = Color.parseColor(it) }
                ds.isUnderlineText = false
            }
        }, spannable.getSpanStart(u), spannable.getSpanEnd(u), 0)
    }
    return spannable
}
fun String.capitalize(): String {
    return this.replaceFirstChar { firstChar -> if (firstChar.isLowerCase()) firstChar.titlecase(
        Locale.getDefault()) else firstChar.toString() }
}
fun String.doubleQuoted() = "\"$this\""
fun String.username()="@$this"
fun String.isValidEmail() =
    !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String?.isNullOrEmptyMy(): Boolean {
    return this.isNullOrEmpty() || this == "null"
}

fun String?.isNotNullOrEmptyMy(): Boolean {
    return this != null && this.isNotEmpty() && this != "null"
}

fun String.addBaseUrl() = "${Consts.BASE_URL}${this}"

fun copyTextToClipboard(copyText: String, label: String, context: Context){
    val clipboard: ClipboardManager? =
        ContextCompat.getSystemService(context, ClipboardManager::class.java)
    val clip = ClipData.newPlainText(label, copyText)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
}