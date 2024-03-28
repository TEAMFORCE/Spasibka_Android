package com.teamforce.thanksapp.data.entities.profile

import com.google.gson.annotations.SerializedName

data class ProfileStatsEntity(
    val id: Int,
    val name: String,
    val amount: Int,
    val sum: Double,
    @SerializedName("percent_from_total_sum")
    val percentFromTotalSum: Float,
    @SerializedName("percent_from_total_amount")
    val percentFromTotalAmount: Float
)
