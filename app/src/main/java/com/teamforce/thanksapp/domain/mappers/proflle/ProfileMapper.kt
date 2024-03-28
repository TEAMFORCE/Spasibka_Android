package com.teamforce.thanksapp.domain.mappers.proflle

import com.teamforce.thanksapp.data.entities.profile.ContactEntity
import com.teamforce.thanksapp.data.entities.profile.PrivilegedEntity
import com.teamforce.thanksapp.data.entities.profile.ProfileBeanEntity
import com.teamforce.thanksapp.data.entities.profile.ProfileEntity
import com.teamforce.thanksapp.data.response.UserListItem.UserBean
import com.teamforce.thanksapp.domain.models.profile.ContactModel
import com.teamforce.thanksapp.domain.models.profile.ProfileBeanModel
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.models.profile.Roles
import com.teamforce.thanksapp.data.entities.profile.*
import com.teamforce.thanksapp.domain.models.profile.*
import com.teamforce.thanksapp.utils.Locals.Lang
import javax.inject.Inject

class ProfileMapper @Inject constructor(
    private val profileBeanMapper: ProfileBeanMapper,
) {
    fun map(from: ProfileEntity): ProfileModel {
        return ProfileModel(
            id = from.id,
            username = from.username ?: "Unknown",
            profile = profileBeanMapper.map(from.profile, from.id, from.privileged),
            rate = from.rate
        )
    }

    fun mapUserProfileModelToProfileBean(from: ProfileModel): UserBean {
        return UserBean(
            userId = from.profile.id.toInt(),
            tgName = getTgName(from.profile.nickname, from.profile.tgName),
            firstname = from.profile.firstname?: "",
            surname = from.profile.surname?: "",
            photo = from.profile.photo,
        )
    }

    fun mapUserProfileEntityToProfileBean(from: ProfileEntity): UserBean {
        return UserBean(
            userId = from.id,
            tgName = getTgName(from.profile.nickname, from.profile.tgName),
            firstname = from.profile.firstname?: "",
            surname = from.profile.surname?: "",
            photo = from.profile.photo,
        )
    }

    private fun getTgName(nickname: String?, tgName: String?): String{
        return if(nickname.isNullOrEmpty()) tgName ?: ""
        else nickname
    }
}

class ProfileBeanMapper @Inject constructor(
    private val contactMapper: ContactMapper
) {
    fun map(from: ProfileBeanEntity, rightId: Int, privileged: List<PrivilegedEntity>?): ProfileBeanModel {
        return ProfileBeanModel(
            id = rightId.toString(),
            idForEdit = from.id,
            contacts = contactMapper.mapList(from.contacts),
            organization = from.organization,
            organizationId = from.organizationId,
            department = from.department,
            tgId = from.tgId,
            tgName = from.tgName,
            photo = from.photo,
            hiredAt = from.hiredAt,
            surname = from.surname,
            firstname = from.firstname,
            middleName = from.middlename,
            nickname = from.nickname,
            jobTitle = from.jobTitle,
            roles = if(privileged == null) null else mapRole(privileged),
            birthday = from.birthday,
            showYearOfBirth = from.showYearOfBirth,
            gender = from.gender,
            isHeadOfACurrentCommunity = if(privileged != null) isHeadOfACurrentDepartment(privileged) else false,
            superuser = from.superuser,
            authThroughVk = from.authMethod == "VK",
            awards = from.awards,
            location = mapLocation(from.locationText, from.latitude, from.longitude),
            status = from.status,
            formatOfWork = from.formatOfWork
        )
    }

    private fun mapLocation(locationText: String?, latitude: Double?, longitude: Double?): LocationModel?{
        return if(locationText != null && latitude != null && longitude != null){
            LocationModel(lat = latitude, lon = longitude, city = locationText, timezone = "")
        }else{
            null
        }
    }

    private fun mapRole(data: List<PrivilegedEntity>): String? {
        val roles = data.map {
            if(!it.roleName.isNullOrEmpty()) it.roleName else ""
        }
        return if(roles.isEmpty()) null
        else roles.joinToString(separator = ", ")
    }

    private fun isHeadOfACurrentDepartment(data: List<PrivilegedEntity>): Boolean{
        val roles: List<String> = data.map {
            if(!it.roleChar.isNullOrEmpty()) it.roleChar
            else ""
        }
        return roles.contains(Roles.ADMIN.value)
    }
}

class ContactMapper @Inject constructor() {
    private fun map(from: ContactEntity): ContactModel {
        return ContactModel(
            id = from.id,
            contactType = from.contact_type,
            contactId = from.contact_id
        )
    }

    fun mapList(from: List<ContactEntity>): List<ContactModel> {
        return from.map {
            map(it)
        }
    }
}

class ProfileSettingsMapper @Inject constructor(
) {
    fun map(from: ProfileSettingsEntity): ProfileSettingsModel {
        return ProfileSettingsModel(
            id = from.details.id,
            contacts = from.details.contacts,
            organization = from.details.organization,
            organizationId = from.details.organizationId,
            department = from.details.department,
            departmentId = from.details.departmentId,
            photo = from.details.photo,
            photos = from.details.photos,
            longitude = from.details.longitude,
            latitude = from.details.latitude,
            locationText = from.details.locationText,
            dateOfBirth = from.details.dateOfBirth,
            showLocation = from.details.showLocation,
            tgId = from.details.tgId,
            tgName = from.details.tgName,
            hiredAt = from.details.hiredAt,
            firedAt = from.details.firedAt,
            surname = from.details.surname,
            firstName = from.details.firstName,
            middleName = from.details.middleName,
            nickname = from.details.nickname,
            status = from.details.status,
            timezone = from.details.timezone,
            jobTitle = from.details.jobTitle,
            gender = from.details.gender,
            showBirthday = from.details.showBirthday,
            verifyData = from.details.verifyData,
            loginAttemptsCount = from.details.loginAttemptsCount,
            vkId = from.details.vkId,
            authMethod = from.details.authMethod,
            language = Lang.fromString(from.details.language),
            person = from.details.person
        )
    }

}