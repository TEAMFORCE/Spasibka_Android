package com.teamforce.thanksapp.domain.usecases

import com.teamforce.thanksapp.domain.mappers.proflle.ProfileMapper
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.repositories.ProfileRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import javax.inject.Inject

class LoadProfileOfAnotherUserUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val profileMapper: ProfileMapper
) {
    suspend operator fun invoke(userId: Int): ResultWrapper<ProfileModel> {
        return when (val result = profileRepository.loadUserProfileOfAnotherUser(userId = userId)) {
            is ResultWrapper.Success -> ResultWrapper.Success(
                profileMapper.map(result.value)
            )
            is ResultWrapper.NetworkError -> ResultWrapper.NetworkError()
            is ResultWrapper.GenericError -> ResultWrapper.GenericError(
                result.code,
                result.error
            )
        }
    }
}