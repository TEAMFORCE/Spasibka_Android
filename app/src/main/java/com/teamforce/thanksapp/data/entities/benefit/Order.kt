package com.teamforce.thanksapp.data.entities.benefit

import com.google.gson.annotations.SerializedName

data class Order(
    val id: Int,
    val quantity: Int,
    @SerializedName("offer_id")
    val offerId: Int,
    val images: List<BenefitItemEntity.Image>?,
    val description: String,
    @SerializedName("actual_to")
    val actualTo: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("current_status")
    val currentStatus: Int,
    val rest: Int,
    val price: Int?,
    val total: Int?,
    val name: String
)
