package com.teamforce.thanksapp.data.entities.auth

import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.data.response.AuthResponse

data class AuthVkResponse(
    val status: Int,
    val details: String?,
    val token: String?,
    val data: String?,
    @SerializedName("organizations_data")
    val organizations: List<AuthResponse.Organization>?,
    val errors: String?
)
