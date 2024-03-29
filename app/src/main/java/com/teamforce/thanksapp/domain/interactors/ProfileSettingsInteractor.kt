package com.teamforce.thanksapp.domain.interactors

import com.teamforce.thanksapp.data.entities.profile.ProfileSettingsEntity
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodRequest
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodResponse
import com.teamforce.thanksapp.domain.models.profile.ProfileSettingsModel
import com.teamforce.thanksapp.domain.repositories.ProfileRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import javax.inject.Inject

class ProfileSettingsInteractor @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend fun createCommunity(request: CreateCommunityWithPeriodRequest): ResultWrapper<CreateCommunityWithPeriodResponse>{
        return profileRepository.createCommunity(request)
    }

    suspend fun loadProfileSettings(): ResultWrapper<ProfileSettingsModel>{
        return profileRepository.getProfileSettings()
    }
}