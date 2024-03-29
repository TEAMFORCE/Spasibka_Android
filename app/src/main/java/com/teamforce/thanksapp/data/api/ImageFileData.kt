package com.teamforce.thanksapp.data.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.presentation.adapter.transactions.AdapterItem
import kotlinx.parcelize.Parcelize

sealed class ImageFileData : Parcelable {
    @Parcelize
    data class FileData(
        @SerializedName("sortIndex")
        val sortIndex: Int,
        @SerializedName("filename")
        val filename: String,
    ) : ImageFileData()

    @Parcelize
    data class ImageData(
        @SerializedName("sortIndex")
        val sortIndex: Int,
        @SerializedName("index")
        val index: Int,
    ) : ImageFileData()
}

fun List<AdapterItem>.toIndexedList(): List<ImageFileData> = mapIndexedNotNull { index, adapterItem ->
    when (adapterItem) {
        is AdapterItem.File -> ImageFileData.FileData(index + 1, adapterItem.fileName)
        is AdapterItem.Image -> ImageFileData.ImageData(index + 1, adapterItem.index)
    }
}


