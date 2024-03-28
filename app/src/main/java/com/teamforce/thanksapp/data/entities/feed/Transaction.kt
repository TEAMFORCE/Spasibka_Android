package com.teamforce.thanksapp.data.entities.feed


import com.google.gson.annotations.SerializedName

data class Transaction(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean,
    @SerializedName("recipient_id")
    val recipientId: Int,
    @SerializedName("recipient_photo")
    val recipientPhoto: String?,
    @SerializedName("recipient_tg_name")
    val recipientTgName: String,
    @SerializedName("recipient_first_name")
    val recipientFirstName: String?,
    @SerializedName("recipient_surname")
    val recipientSurname: String?,
    @SerializedName("sender_id")
    val senderId: Int?,
    @SerializedName("sender_tg_name")
    val senderTgName: String?,
    @SerializedName("sender_first_name")
    val senderFirstName: String?,
    @SerializedName("sender_surname")
    val senderSurname: String?,
    @SerializedName("tags")
    val tags: List<Tag>,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_liked")
    val userLiked: Boolean
)

/*
  "recipient_first_name": "Елизавета",
      "recipient_surname": "Михеева",
 */