package com.teamforce.thanksapp.data.repository

import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.auth.AuthVkRequest
import com.teamforce.thanksapp.data.entities.auth.AuthVkResponse
import com.teamforce.thanksapp.data.entities.auth.ChooseOrgThroughAuthRequest
import com.teamforce.thanksapp.domain.repositories.AuthRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
) : AuthRepository {

    override suspend fun authThroughVk(
        accessToken: String,
        sharedKey: String?
    ): ResultWrapper<AuthVkResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.authThroughVk(AuthVkRequest(accessToken, sharedKey))
        }
    }

    override suspend fun chooseOrgThroughVk(
        userId: Int,
        orgId: Int?,
        accessToken: String
    ): ResultWrapper<AuthVkResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.chooseOrgThroughVk(
                ChooseOrgThroughAuthRequest(
                    userId,
                    orgId,
                    accessToken
                )
            )
        }
    }
}