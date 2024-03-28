package com.teamforce.thanksapp.data.request.settings

data class CreateCommunityWithPeriodResponse(
    val invite_link: String,
    val organization_id: Int,
    val status: String
)