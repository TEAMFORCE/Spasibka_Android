package com.teamforce.thanksapp.data.api

import com.google.gson.annotations.SerializedName

class SectionUpdateRequest(
    val name: String,
    @SerializedName("upper_sections")
    val upperSections: List<Int>,
    @SerializedName("scope")
    val section: Long? = null,
)
