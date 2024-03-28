package com.teamforce.thanksapp.data.entities.profile

import com.google.gson.annotations.SerializedName

data class ProfileSettingsForRequestEntity(
    @SerializedName("tgName")
    val tg_name: String? = null,
    val surname: String? = null,
    @SerializedName("firstName")
    val first_name: String? = null,
    val gender: String? = null,
    @SerializedName("middleName")
    val middle_name: String? = null,
    val nickname: String? = null,
    val status: String? = null,
    @SerializedName("showBirthday")
    val show_birthday: Boolean? = null,
    val timezone: Int? = null,
    @SerializedName("dateOfBirth")
    val date_of_birth: String? = null,
    @SerializedName("jobTitle")
    val job_title: String? = null,
    val language: String? = null
)
