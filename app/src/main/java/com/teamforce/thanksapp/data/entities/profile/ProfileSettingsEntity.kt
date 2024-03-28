package com.teamforce.thanksapp.data.entities.profile
import com.google.gson.annotations.SerializedName

data class ProfileSettingsEntity(
    val status: Int,
    val details: Details
){
    data class Details(
        val id: Int,
        val contacts: List<ContactEntity?>?,
        val organization: String?,
        @SerializedName("organization_id")
        val organizationId: Int?,
        val department: String?,
        @SerializedName("department_id")
        val departmentId: Int?,
        val photo: String?,
        val photos: String?,
        val longitude: Double?,
        val latitude: Double?,
        @SerializedName("location_text")
        val locationText: String?,
        @SerializedName("date_of_birth")
        val dateOfBirth: String?,
        @SerializedName("show_location")
        val showLocation: Boolean,
        @SerializedName("tg_id")
        val tgId: String?,
        @SerializedName("tg_name")
        val tgName: String?,
        @SerializedName("hired_at")
        val hiredAt: String?,
        @SerializedName("fired_at")
        val firedAt: String?,
        val surname: String?,
        @SerializedName("first_name")
        val firstName: String?,
        @SerializedName("middle_name")
        val middleName: String?,
        val nickname: String?,
        val status: String?,
        val timezone: String?,
        @SerializedName("job_title")
        val jobTitle: String?,
        val gender: String?,
        @SerializedName("show_birthday")
        val showBirthday: Boolean,
        @SerializedName("verify_data")
        val verifyData: String?,
        @SerializedName("login_attempts_count")
        val loginAttemptsCount: Int?,
        @SerializedName("vk_id")
        val vkId: Long?,
        @SerializedName("auth_method")
        val authMethod: String?,
        val language: String?,
        val person: Int?
    )
}



