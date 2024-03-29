package com.teamforce.thanksapp.data.response.onboarding

import com.google.gson.annotations.SerializedName

data class InvitationOrganizationResponse(
    val status: String,
    @SerializedName("organization_id")
    val organizationId: Int,
    @SerializedName("organization_name")
    val organizationName: String
)
