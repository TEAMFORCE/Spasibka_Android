package com.teamforce.thanksapp.data.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("tg_name") val  tgName: String? = null,
    @SerializedName("surname") val  surname: String? = null,
    @SerializedName("first_name") val  firstName: String? = null,
    @SerializedName("middle_name") val middleName: String? = null,
    @SerializedName("nickname") val nickname: String? = null,
    @SerializedName("status") val formatOfWork: String?,
    @SerializedName("secondary_status") val status: String? = null,
    @SerializedName("date_of_birth") val birthday: String? = null,
    @SerializedName("show_birthday") val showYearOfBirth: Boolean? = null,
    @SerializedName("gender") val gender: String? = null
    )
