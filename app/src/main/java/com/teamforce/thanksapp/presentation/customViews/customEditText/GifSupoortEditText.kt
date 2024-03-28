package com.teamforce.thanksapp.presentation.customViews.customEditText

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat

class GifSupportEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = androidx.appcompat.R.attr.editTextStyle,
) : AppCompatEditText(
    context, attrs, defStyle
) {
    private var imgTypeString: Array<String> = arrayOf(
        "image/gif", "image/png"
    )
    private var keyBoardInputCallbackListener: KeyBoardInputCallbackListener? = null

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val ic: InputConnection? = super.onCreateInputConnection(outAttrs)
        EditorInfoCompat.setContentMimeTypes(
            outAttrs, imgTypeString

        )
        val callback = InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, opts ->
            val lacksPermission = (flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0
            // read and display inputContentInfo asynchronously
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && lacksPermission) {
                try {
                    inputContentInfo.requestPermission()
                } catch (e: Exception) {
                    Log.e(TAG, "inputContentInfo.requestPermission: error")
                    return@OnCommitContentListener false // return false if failed
                }
            }

            var supported = false
            for (mimeType in imgTypeString) {
                if (inputContentInfo.description.hasMimeType(mimeType)) {
                    supported = true
                    break
                } else {
                    Log.d(TAG, "onCreateInputConnection: $mimeType not support")
                }
            }
            if (!supported) {
                return@OnCommitContentListener false
            }
            if (keyBoardInputCallbackListener != null) {
                InputConnectionCompat.commitContent(
                    ic!!, outAttrs, inputContentInfo, flags, null
                )
                keyBoardInputCallbackListener?.onCommitContent(inputContentInfo, flags, opts.apply {})
            }
            true
        }

        return InputConnectionCompat.createWrapper(ic!!, outAttrs, callback)
    }

    fun interface KeyBoardInputCallbackListener {
        fun onCommitContent(
            inputContentInfo: InputContentInfoCompat?, flags: Int, opts: Bundle?
        )
    }

    fun setKeyBoardInputCallbackListener(callbackListener: KeyBoardInputCallbackListener?) {
        keyBoardInputCallbackListener = callbackListener
    }
    companion object {
        private const val TAG = "GifSupportEditText"
    }
}