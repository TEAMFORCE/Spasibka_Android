package com.teamforce.thanksapp.data.entities.benefit

import com.google.gson.annotations.SerializedName

data class OfferInCart(
    val id: Int,
    val quantity: Int,
    @SerializedName("offer_id")
    val offerId: Int,
    val images: List<BenefitItemEntity.Image>?,
    @SerializedName("actual_to")
    val actualTo: String?,
    @SerializedName("is_chosen")
    val isChecked: Boolean,
    val rest: Int,
    val price: Int?,
    val total: Int,
    val name: String
)
