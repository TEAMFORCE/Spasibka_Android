package com.teamforce.thanksapp.data.entities.Stickers

import com.google.gson.annotations.SerializedName

data class StickerEntity(
    val id: Int,
    @SerializedName("stickerpackid")
    val stickerpackId: Int,
    val image: String
)
