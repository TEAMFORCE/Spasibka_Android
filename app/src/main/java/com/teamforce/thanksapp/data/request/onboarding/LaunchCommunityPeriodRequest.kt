package com.teamforce.thanksapp.data.request.onboarding

data class LaunchCommunityPeriodRequest(
    val start_date: String,
    val end_date: String,
    val default: Boolean = false,
    val distr_amount: Int,
    val head_distr_amount: Int,
)
