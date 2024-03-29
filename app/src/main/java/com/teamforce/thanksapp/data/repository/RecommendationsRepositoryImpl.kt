package com.teamforce.thanksapp.data.repository

import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.domain.mappers.recommendations.RecommendationsMapper
import com.teamforce.thanksapp.domain.models.recommendations.RecommendModel
import com.teamforce.thanksapp.domain.repositories.RecommendationsRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.mapWrapperData
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class RecommendationsRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val recommendationsMapper: RecommendationsMapper
    ): RecommendationsRepository {
    override suspend fun getRecommendations(): ResultWrapper<List<RecommendModel>> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.getRecommendations()
        }.mapWrapperData { recommendationsMapper.mapRecommendsList(it.data) }
    }
}