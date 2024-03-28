package com.teamforce.thanksapp.domain.models.profile

import com.teamforce.thanksapp.data.entities.profile.ContactEntity
import com.teamforce.thanksapp.utils.Locals.Lang

data class ProfileSettingsModel(
    val id: Int,
    val contacts: List<ContactEntity?>?,
    val organization: String?,
    val organizationId: Int?,
    val department: String?,
    val departmentId: Int?,
    val photo: String?,
    val photos: String?,
    val longitude: Double?,
    val latitude: Double?,
    val locationText: String?,
    val dateOfBirth: String?,
    val showLocation: Boolean,
    val tgId: String?,
    val tgName: String?,
    val hiredAt: String?,
    val firedAt: String?,
    val surname: String?,
    val firstName: String?,
    val middleName: String?,
    val nickname: String?,
    val status: String?,
    val timezone: String?,
    val jobTitle: String?,
    val gender: String?,
    val showBirthday: Boolean,
    val verifyData: String?,
    val loginAttemptsCount: Int?,
    val vkId: Long?,
    val authMethod: String?,
    val language: Lang,
    val person: Int?
)
