package com.teamforce.thanksapp.data.request.awards

import com.google.gson.annotations.SerializedName

data class GainAwardRequest(
    @SerializedName("user_id")
    val userId: Long?,
    @SerializedName("award_type_id")
    val awardId: Long
)
