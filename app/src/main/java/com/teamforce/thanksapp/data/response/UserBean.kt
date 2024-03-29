package com.teamforce.thanksapp.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize



sealed class UserListItem {
    // Пустой конструктор
    constructor()
    @Parcelize
    data class UserBean(
        @SerializedName("user_id") val userId: Int,
        @SerializedName("tg_name") val tgName: String,
        @SerializedName("name") val firstname: String,
        @SerializedName("surname") val surname: String?,
        @SerializedName("photo") val photo: String?
    ) : UserListItem(), Parcelable

    object NewTransactionBtn: UserListItem()
}