package com.teamforce.thanksapp.data.request

data class GetReactionsRequest(
    val transaction_id: Int? = null,
    val challenge_id: Int? = null,
    val challenge_report_id: Int? = null,
    val offer_id: Int? = null,
    val order_id: Int? = null,
    val comment_id: Int? = null,
    val offer_review_id: Int? = null,
    val offset: Int,
    val limit: Int,
    val include_name: Boolean = true,
    val include_code: Boolean = true
)
