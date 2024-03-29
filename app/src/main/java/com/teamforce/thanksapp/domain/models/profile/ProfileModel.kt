package com.teamforce.thanksapp.domain.models.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.data.entities.profile.AwardModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileModel(
    val id: Int,
    val username: String,
    val profile: ProfileBeanModel,
    val rate: Int
) : Parcelable

@Parcelize
data class ProfileBeanModel(
    val id: String,
    val idForEdit: String,
    val contacts: List<ContactModel>,
    val organization: String?,
    val organizationId: Int?,
    val department: String?,
    val tgId: String?,
    val tgName: String?,
    val photo: String?,
    val hiredAt: String?,
    val birthday: String?,
    val surname: String?,
    val firstname: String?,
    val middleName: String?,
    val nickname: String?,
    val jobTitle: String?,
    val formatOfWork: String?,
    val status: String?,
    val gender: String?,
    val showYearOfBirth: Boolean = false,
    val roles: String?,
    val isHeadOfACurrentCommunity: Boolean = false,
    val superuser: Boolean = false,
    val authThroughVk: Boolean = false,
    val awards: List<AwardModel>,
    val location: LocationModel?
) : Parcelable

@Parcelize
data class ContactModel(
    val id: Int?,
    val contactType: String,
    var contactId: String
) : Parcelable

enum class Status(val value: String){
   VACATION("H"), SICK_LEAVE("S"), WORK("")
}

enum class FormatOfWork(val value: String){
    OFFICE("O"), REMOTE("D")
}

enum class Roles(val value: String){
    ADMIN("A"), CONTROLLER("C"), STEWARD("M")
}
