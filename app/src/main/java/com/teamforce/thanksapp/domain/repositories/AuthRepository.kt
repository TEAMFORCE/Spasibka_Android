package com.teamforce.thanksapp.domain.repositories

import com.teamforce.thanksapp.data.entities.auth.AuthVkResponse
import com.teamforce.thanksapp.utils.ResultWrapper

interface AuthRepository {

    suspend fun authThroughVk(accessToken: String, sharedKey: String?): ResultWrapper<AuthVkResponse>

    suspend fun chooseOrgThroughVk(userId: Int, orgId: Int?, accessToken: String): ResultWrapper<AuthVkResponse>
}