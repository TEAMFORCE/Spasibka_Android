package com.teamforce.thanksapp.data.repository

import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.profile.*
import com.teamforce.thanksapp.data.request.ChangeOrgRequest
import com.teamforce.thanksapp.data.request.UpdateProfileRequest
import com.teamforce.thanksapp.data.request.VerificationRequestForChangeOrg
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodRequest
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodResponse
import com.teamforce.thanksapp.data.response.*
import com.teamforce.thanksapp.data.response.profile.ProfileSettingsResponse
import com.teamforce.thanksapp.domain.mappers.proflle.ProfileMapper
import com.teamforce.thanksapp.domain.mappers.proflle.ProfileSettingsMapper
import com.teamforce.thanksapp.domain.models.profile.FormatOfWork
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.models.profile.ProfileSettingsModel
import com.teamforce.thanksapp.domain.repositories.ProfileRepository
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Locals.Lang
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val profileMapper: ProfileMapper,
    private val profileSettingsMapper: ProfileSettingsMapper
) : ProfileRepository {
    override suspend fun loadUserProfile(): ResultWrapper<ProfileModel> {
        return safeApiCall(Dispatchers.IO) {
            profileMapper.map(thanksApi.getProfile())
        }
    }

    override suspend fun loadUpdateProfile(
        userId: String,
        tgName: String?, surname: String?,
        firstName: String?, middleName: String?, nickname: String?, status: String?, formatOfWork: String?,
        birthDay: String?, showYearOfBirth: Boolean, gender: String?
    ): ResultWrapper<UpdateProfileResponse> {
        val updateProfileRequest = UpdateProfileRequest(
            tgName, surname,
            firstName, middleName, nickname, formatOfWork, status, birthDay, showYearOfBirth, gender
        )
        return safeApiCall(Dispatchers.IO) {
            thanksApi.updateProfile(userId, updateProfileRequest)
        }
    }

    override suspend fun updateStatus(
        userId: String,
        status: String?,
        formatOfWork: String?
    ): ResultWrapper<UpdateProfileResponse> {
        val updateProfileRequest = UpdateProfileRequest(
           status = status, formatOfWork = formatOfWork
        )
        return safeApiCall(Dispatchers.IO) {
            thanksApi.updateProfile(userId, updateProfileRequest)
        }
    }

    override suspend fun loadUserProfileOfAnotherUser(userId: Int): ResultWrapper<ProfileEntity> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getAnotherProfile(user_Id = userId)
        }
    }

    override suspend fun updateUserAvatar(
        userId: String,
        filePath: String,
        filePathCropped: String
    ): ResultWrapper<PutUserAvatarResponse> {
        return safeApiCall(Dispatchers.IO) {
            val file = File(filePath)
            val fileCropped = File(filePathCropped)
            val requestFile: RequestBody =
                RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            val requestFileCropped =
                RequestBody.create("multipart/form-data".toMediaTypeOrNull(), fileCropped)
            val bodyCropped = MultipartBody.Part.createFormData(
                "cropped_photo",
                fileCropped.name,
                requestFileCropped
            )
            val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)
            thanksApi.putUserAvatar(userId, body, bodyCropped)
            // Отправляю ориг и обрезку на бек, протестировать с Андреем
        }
    }

    override suspend fun getOrganizations(): ResultWrapper<List<OrganizationModel>> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getOrganizations()
        }
    }

    override suspend fun changeOrganization(organizationId: Int, accessToken: String?): ResultWrapper<ChangeOrgResponse> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.changeOrganization(ChangeOrgRequest(organizationId, accessToken))
        }
    }

    override suspend fun changeOrganizationVerifyWithTelegram(
        xId: String,
        xCode: String,
        code: String,
        orgCode: String
    ): ResultWrapper<VerificationResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.changeOrganizationVerifyWithTelegram(
                VerificationRequestForChangeOrg(
                    code = code,
                    xCode = xCode,
                    orgId = orgCode,
                    tgCode = xId,
                    email = null
                )
            )
        }
    }

    override suspend fun changeOrganizationVerifyWithEmail(
        xEmail: String,
        xCode: String,
        code: String,
        orgCode: String
    ): ResultWrapper<VerificationResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.changeOrganizationVerifyWithEmail(
                VerificationRequestForChangeOrg(
                    code = code,
                    xCode = xCode,
                    orgId = orgCode,
                    tgCode = null,
                    email = xEmail
                )
            )
        }
    }

    override suspend fun getProfileStats(
        senderOnly: Int?,
        recipientOnly: Int?
    ): ResultWrapper<List<ProfileStatsEntity>> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getProfileStats(senderOnly, recipientOnly)
        }
    }

    override suspend fun getOrgSettings(): ResultWrapper<GetOrgSettings> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getOrgSettings()
        }
    }

    override suspend fun createCommunity(request: CreateCommunityWithPeriodRequest): ResultWrapper<CreateCommunityWithPeriodResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.createCommunity(request)
        }
    }

    override suspend fun updateProfileLang(currentLang: Lang): ResultWrapper<ProfileSettingsResponse> {
        return safeApiCall(Dispatchers.IO){
            val request = ProfileSettingsForRequestEntity(
                language = currentLang.lang
            )
            thanksApi.profileSettingsUpdate(request)
        }
    }

    override suspend fun getProfileSettings(): ResultWrapper<ProfileSettingsModel> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.getProfileSettings()
        }.mapWrapperData { profileSettingsMapper.map(it) }

    }


}