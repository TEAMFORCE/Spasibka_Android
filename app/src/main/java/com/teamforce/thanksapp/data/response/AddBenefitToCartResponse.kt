package com.teamforce.thanksapp.data.response

import com.google.gson.annotations.SerializedName
import java.text.FieldPosition

data class AddBenefitToCartResponse(
    val id: Int,
    @SerializedName("offer_id")
    val offerId: Int,
    @SerializedName("marketplace_id")
    val marketplaceId: Int,
    @SerializedName("order_status")
    val orderStatus: Int,
    val quantity: Int,
    val price: Int,
    val amount: Int,
    val position: Int?
)
