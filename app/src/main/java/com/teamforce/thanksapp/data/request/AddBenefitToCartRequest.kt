package com.teamforce.thanksapp.data.request

import com.google.gson.annotations.SerializedName

data class AddBenefitToCartRequest(
    @SerializedName("offer_id")
    val offerId: Int,
    val quantity: Int,
    val status: String?
)