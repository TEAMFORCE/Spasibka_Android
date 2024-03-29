package com.teamforce.thanksapp.data.entities.auth

import com.google.gson.annotations.SerializedName

data class ChooseOrgThroughAuthRequest(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("organization_id")
    val organizationId: Int?,
    @SerializedName("access_token")
    val accessToken: String
)
