package com.teamforce.thanksapp.data.request

import com.google.gson.annotations.SerializedName

data class GetCommentsRequest(
    @SerializedName("transaction_id")
    val transaction_id: Int? = null,
    @SerializedName("challenge_id")
    val challenge_id: Int? = null,
    @SerializedName("challenge_report_id")
    val challenge_report_id: Int? = null,
    @SerializedName("offer_id")
    val offerId: Int? = null,
    @SerializedName("include_name")
    val include_name: Boolean = true,
    @SerializedName("is_reverse_order")
    val isReversed: Boolean = true,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("offset")
    val offset: Int
)