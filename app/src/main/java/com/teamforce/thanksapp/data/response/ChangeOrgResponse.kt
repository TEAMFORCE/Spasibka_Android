package com.teamforce.thanksapp.data.response

import com.google.gson.annotations.SerializedName

data class ChangeOrgResponse(
    val status: String,
    val target: String,
    @SerializedName("organization_id")
    val orgId: String,
    @SerializedName("X-Code")
    val xCode: String,
    @SerializedName("tg_id")
    val tgCode: String?,
    val email: String?,
    val token: String?

)
