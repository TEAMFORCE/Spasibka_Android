package com.teamforce.thanksapp.domain.repositories

import com.teamforce.thanksapp.data.entities.profile.OrganizationModel
import com.teamforce.thanksapp.data.entities.profile.ProfileEntity
import com.teamforce.thanksapp.data.entities.profile.ProfileSettingsEntity
import com.teamforce.thanksapp.data.entities.profile.ProfileStatsEntity
import com.teamforce.thanksapp.data.request.UpdateProfileRequest
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodRequest
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodResponse
import com.teamforce.thanksapp.data.response.*
import com.teamforce.thanksapp.data.response.profile.ProfileSettingsResponse
import com.teamforce.thanksapp.domain.models.profile.FormatOfWork
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.models.profile.ProfileSettingsModel
import com.teamforce.thanksapp.utils.Locals.Lang
import com.teamforce.thanksapp.utils.ResultWrapper
import retrofit2.Call

interface ProfileRepository {
    suspend fun loadUserProfile(): ResultWrapper<ProfileModel>

    suspend fun loadUpdateProfile(
        userId: String,
        tgName: String?, surname: String?,
        firstName: String?, middleName: String?, nickname: String?, status: String?, formatOfWork: String?,
        birthDay: String?, showYearOfBirth: Boolean, gender: String?
    ): ResultWrapper<UpdateProfileResponse>

    suspend fun updateStatus(
        userId: String, status: String?, formatOfWork: String?

        ): ResultWrapper<UpdateProfileResponse>

    suspend fun loadUserProfileOfAnotherUser(userId: Int): ResultWrapper<ProfileEntity>

    suspend fun updateUserAvatar(
        userId: String,
        filePath: String,
        filePathCropped: String
    ): ResultWrapper<PutUserAvatarResponse>

    suspend fun getOrganizations(): ResultWrapper<List<OrganizationModel>>

    suspend fun changeOrganization(
        organizationId: Int,
        accessToken: String? = null
    ): ResultWrapper<ChangeOrgResponse>

    suspend fun changeOrganizationVerifyWithTelegram(
        xId: String, xCode: String, code: String, orgCode: String
    ): ResultWrapper<VerificationResponse>

    suspend fun changeOrganizationVerifyWithEmail(
        xEmail: String, xCode: String, code: String, orgCode: String
    ): ResultWrapper<VerificationResponse>

    suspend fun getProfileStats(
        senderOnly: Int?,
        recipientOnly: Int?
    ): ResultWrapper<List<ProfileStatsEntity>>


    suspend fun getOrgSettings(): ResultWrapper<GetOrgSettings>

    suspend fun createCommunity(request: CreateCommunityWithPeriodRequest): ResultWrapper<CreateCommunityWithPeriodResponse>

    suspend fun updateProfileLang(currentLang: Lang): ResultWrapper<ProfileSettingsResponse>

    suspend fun getProfileSettings(): ResultWrapper<ProfileSettingsModel>
}