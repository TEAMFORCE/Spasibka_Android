package com.teamforce.thanksapp.domain.usecases

import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.repositories.ProfileRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import javax.inject.Inject

class LoadProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke(): ResultWrapper<ProfileModel> {
        return when (val result = profileRepository.loadUserProfile()) {
            is ResultWrapper.Success -> {
                userDataRepository.saveUserAvatar(result.value.profile.photo)
                userDataRepository.saveProfileId(result.value.profile.id)
                userDataRepository.saveUsername(result.value.profile.tgName)
                userDataRepository.saveUserIsAdmin(result.value.profile.isHeadOfACurrentCommunity)
                userDataRepository.saveUserCurrentOrgId(result.value.profile.organizationId)
                userDataRepository.saveUserIsSuperUser(result.value.profile.superuser)
                userDataRepository.saveName(result.value.profile.firstname)
                userDataRepository.saveSurname(result.value.profile.surname)
                ResultWrapper.Success(result.value)
            }
            is ResultWrapper.NetworkError -> ResultWrapper.NetworkError()
            is ResultWrapper.GenericError -> ResultWrapper.GenericError(
                result.code,
                result.error
            )
        }
    }
}