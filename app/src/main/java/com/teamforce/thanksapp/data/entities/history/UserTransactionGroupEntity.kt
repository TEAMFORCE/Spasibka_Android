package com.teamforce.thanksapp.data.entities.history

import com.google.gson.annotations.SerializedName

data class UserTransactionGroupEntity(
    @SerializedName("user_id")
    val userId: Long,
    val received: Int,
    val waiting: Int,
    val ready: Int,
    val type: String,
    @SerializedName("type_name")
    val typeName: String?,
    val date: String, // "2023-05-03"
    @SerializedName("user_photo")
    val userPhoto: String?,
    @SerializedName("first_name")
    val firstName: String?,
    val surname: String?,
    @SerializedName("tg_name")
    val tgName: String?,
    val nickname: String?
)

/*
 "user_id": 36,
      "date": "2023-05-03",
      "received": 1,
      "waiting": 0,
      "ready": 5,
      "user_photo": "/media/users_photo/FZJaQbqX0AEmf8y_UNwwoG7_thumb.jpeg",
      "first_name": "Петр",
      "surname": "Петров",
      "tg_name": "-test-",
      "nickname": "test"
 */
