package com.teamforce.thanksapp.data.response.admin

data class GetCurrentPeriodResponse(
    val id: Int,
    val start_date: String,
    val end_date: String,
    val name: String,
    val organization_id: Int
)
