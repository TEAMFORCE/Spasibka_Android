package com.teamforce.thanksapp.data.request

import com.google.gson.annotations.SerializedName

data class GetEmployeesRequest(
    val name: String?,
    @SerializedName("job_title")
    val jobTitle: String?,
    @SerializedName("in_office")
    val inOffice: Int? = 0,
    @SerializedName("on_holiday")
    val onHoliday: Int? = 0,
    @SerializedName("fired_at")
    val firedAt: Int? = 0,
    val offset: Int = 0,
    val limit: Int = 0,
    val departments: List<Long> = listOf()
)
