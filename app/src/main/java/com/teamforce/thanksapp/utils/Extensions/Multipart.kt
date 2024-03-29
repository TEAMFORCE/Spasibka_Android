package com.teamforce.thanksapp.utils.Extensions

import android.content.Context
import android.net.Uri
import com.teamforce.thanksapp.utils.getFilePathFromUri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun Uri.toMultipart(context: Context, filename: String? = null): MultipartBody.Part {
    val path = getFilePathFromUri(context, this, false)
    val file = File(path)
    val requestFile: RequestBody =
        file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("photos", filename ?: file.name, requestFile)
}
