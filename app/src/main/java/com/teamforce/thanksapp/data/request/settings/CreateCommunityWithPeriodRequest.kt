package com.teamforce.thanksapp.data.request.settings

data class CreateCommunityWithPeriodRequest(
    val organization_name: String,
    val period_start_date: String,
    val period_end_date: String,
    val users_start_balance: Int,
    val owner_start_balance: Int
)
