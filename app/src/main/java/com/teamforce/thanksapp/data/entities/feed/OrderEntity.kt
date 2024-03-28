package com.teamforce.thanksapp.data.entities.feed

import com.google.gson.annotations.SerializedName

data class OrderEntity(
    @SerializedName("id")
    val orderId: Long,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("user_liked")
    var userLiked: Boolean,
    @SerializedName("customer_photo")
    val customerPhoto: String?,
    @SerializedName("customer_first_name")
    val customerFirstName: String?,
    @SerializedName("customer_surname")
    val customerSurname: String?,
    @SerializedName("customer_id")
    val customerId: Int,
    @SerializedName("customer_tg_name")
    val customerTgName: String?,
)

data class DataOfOfferEntity(
    @SerializedName("offer_id")
    val offerId: Long,
    @SerializedName("offer_name")
    val offerName: String?,
    @SerializedName("marketplace_id")
    val marketplaceId: Long?
)