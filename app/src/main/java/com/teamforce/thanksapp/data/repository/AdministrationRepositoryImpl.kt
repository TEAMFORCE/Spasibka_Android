package com.teamforce.thanksapp.data.repository

import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.request.onboarding.CreateCommunityRequest
import com.teamforce.thanksapp.data.request.onboarding.LaunchCommunityPeriodRequest
import com.teamforce.thanksapp.data.response.admin.GetCurrentPeriodResponse
import com.teamforce.thanksapp.data.response.onboarding.CreateCommunityResponse
import com.teamforce.thanksapp.data.response.onboarding.InvitationOrganizationResponse
import com.teamforce.thanksapp.data.response.onboarding.InviteLinkResponse
import com.teamforce.thanksapp.data.response.onboarding.LaunchCommunityPeriodResponse
import com.teamforce.thanksapp.domain.repositories.AdministrationRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


class AdministrationRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
) : AdministrationRepository {

    override suspend fun createCommunity(name: String): ResultWrapper<CreateCommunityResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.createCommunity(CreateCommunityRequest(name))
        }
    }

    override suspend fun launchCommunityPeriod(
        startDate: String,
        endDate: String,
        startBalance: Int,
        startAdminBalance: Int
    ): ResultWrapper<LaunchCommunityPeriodResponse> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.launchCommunityPeriod(LaunchCommunityPeriodRequest(
                start_date = startDate,
                end_date = endDate,
                distr_amount = startBalance,
                head_distr_amount = startAdminBalance
            ))
        }
    }

    override suspend fun getInviteLink(organizationId: Int?): ResultWrapper<InviteLinkResponse> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.getInviteLink(organizationId)
        }
    }

    override suspend fun getOrgByInvitation(invite: String): ResultWrapper<InvitationOrganizationResponse> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.getOrgByInvitation(invite)
        }
    }

    override suspend fun getCurrentPeriod(): ResultWrapper<GetCurrentPeriodResponse> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.getCurrentPeriod()
        }
    }

}