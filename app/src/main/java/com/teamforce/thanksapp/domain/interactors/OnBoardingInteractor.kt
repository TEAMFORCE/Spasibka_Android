package com.teamforce.thanksapp.domain.interactors

import com.teamforce.thanksapp.data.response.admin.GetCurrentPeriodResponse
import com.teamforce.thanksapp.data.response.onboarding.CreateCommunityResponse
import com.teamforce.thanksapp.data.response.onboarding.InvitationOrganizationResponse
import com.teamforce.thanksapp.data.response.onboarding.InviteLinkResponse
import com.teamforce.thanksapp.data.response.onboarding.LaunchCommunityPeriodResponse
import com.teamforce.thanksapp.domain.repositories.AdministrationRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import javax.inject.Inject

class OnBoardingInteractor @Inject constructor(
    private val administrationRepository: AdministrationRepository
) {
    suspend fun createCommunity(name: String): ResultWrapper<CreateCommunityResponse>{
        return administrationRepository.createCommunity(name)
    }

    suspend fun launchCommunityPeriod(startDate: String, endDate: String, startBalance: Int, startAdminBalance: Int): ResultWrapper<LaunchCommunityPeriodResponse>{
        return administrationRepository.launchCommunityPeriod(startDate, endDate, startBalance, startAdminBalance)
    }

    suspend fun getInviteLink(organizationId: Int?): ResultWrapper<InviteLinkResponse>{
        return administrationRepository.getInviteLink(organizationId)
    }

    suspend fun getOrgByInvitation(invite: String): ResultWrapper<InvitationOrganizationResponse>{
        return administrationRepository.getOrgByInvitation(invite)
    }

    suspend fun getCurrentPeriod(): ResultWrapper<GetCurrentPeriodResponse>{
        return administrationRepository.getCurrentPeriod()
    }
}