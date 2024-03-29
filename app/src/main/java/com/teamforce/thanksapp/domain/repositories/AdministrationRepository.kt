package com.teamforce.thanksapp.domain.repositories

import com.teamforce.thanksapp.data.response.admin.GetCurrentPeriodResponse
import com.teamforce.thanksapp.data.response.onboarding.CreateCommunityResponse
import com.teamforce.thanksapp.data.response.onboarding.InvitationOrganizationResponse
import com.teamforce.thanksapp.data.response.onboarding.InviteLinkResponse
import com.teamforce.thanksapp.data.response.onboarding.LaunchCommunityPeriodResponse
import com.teamforce.thanksapp.utils.ResultWrapper

interface AdministrationRepository {

   suspend fun createCommunity(name: String): ResultWrapper<CreateCommunityResponse>

   suspend fun launchCommunityPeriod(startDate: String, endDate: String, startBalance: Int, startAdminBalance: Int): ResultWrapper<LaunchCommunityPeriodResponse>

   suspend fun getInviteLink(organizationId: Int?): ResultWrapper<InviteLinkResponse>

   suspend fun getOrgByInvitation(invite: String): ResultWrapper<InvitationOrganizationResponse>

   suspend fun getCurrentPeriod(): ResultWrapper<GetCurrentPeriodResponse>
}