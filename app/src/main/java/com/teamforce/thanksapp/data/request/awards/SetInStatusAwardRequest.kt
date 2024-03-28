package com.teamforce.thanksapp.data.request.awards

import com.google.gson.annotations.SerializedName

data class SetInStatusAwardRequest(
    @SerializedName("award_id")
    val awardId: Long?
)
