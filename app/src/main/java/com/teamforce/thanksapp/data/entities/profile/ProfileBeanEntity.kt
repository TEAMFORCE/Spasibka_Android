package com.teamforce.thanksapp.data.entities.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.data.network.models.Contact
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileBeanEntity(
    @SerializedName("id")
    val id: String,
    @SerializedName("contacts")
    val contacts: List<ContactEntity>,
    @SerializedName("organization")
    val organization: String?,
    @SerializedName("organization_id")
    val organizationId: Int?,
    @SerializedName("department")
    val department: String?,
    @SerializedName("date_of_birth") val birthday: String?,
    @SerializedName("tg_id") val tgId: String?,
    @SerializedName("tg_name") val tgName: String?,
    @SerializedName("photo") val photo: String?,
    @SerializedName("hired_at") val hiredAt: String?,
    @SerializedName("surname") val surname: String?,
    @SerializedName("first_name") val firstname: String?,
    @SerializedName("middle_name") val middlename: String?,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("job_title") val jobTitle: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("status") val formatOfWork: String?,
    @SerializedName("secondary_status") val status: String?,
    @SerializedName("show_birthday") val showYearOfBirth: Boolean = false,
    @SerializedName("superuser") val superuser: Boolean = false,
    @SerializedName("auth_method") val authMethod: String,
    @SerializedName("location_text") val locationText: String?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("latitude") val latitude: Double?,
    val awards: List<AwardModel>
): Parcelable


@Parcelize
data class AwardModel(
    val id: Long,
    val name: String,
    val cover: String?
): Parcelable
