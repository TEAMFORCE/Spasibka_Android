package com.teamforce.thanksapp.utils

import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.teamforce.thanksapp.R
import java.io.ByteArrayOutputStream

fun Intent.createBitmapFromResult(activity: Activity): Bitmap? {
    val intentBundle = this.extras
    val intentUri = this.data
    var bitmap: Bitmap? = null
    if (intentBundle != null) {
        bitmap = (intentBundle.get("data") as? Bitmap)?.apply {
            compress(Bitmap.CompressFormat.JPEG, 75, ByteArrayOutputStream())
        }
    }

    if (bitmap == null && intentUri != null) {
        intentUri.let { bitmap = BitmapUtils.decodeBitmap(intentUri, activity) }
    }
    return bitmap
}

/**
 * Открывает диалоговое окно чтобы скопировать/поделиться переданным текстом
 *
 * @param copyText текст, которым делимся
 * @return true, если текст успешно скопирован/поделен, false в противном случае
 */
fun copyTextToClipboardViaIntent(copyText: String, context: Context, label: String = ""): Boolean {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, copyText)
        type = "text/plain"
    }

    return try {
        context.startActivity(Intent.createChooser(sendIntent, label))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}