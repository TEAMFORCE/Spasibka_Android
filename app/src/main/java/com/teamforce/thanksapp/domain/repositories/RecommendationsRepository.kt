package com.teamforce.thanksapp.domain.repositories

import com.teamforce.thanksapp.domain.models.recommendations.RecommendModel
import com.teamforce.thanksapp.utils.ResultWrapper

interface RecommendationsRepository {

    suspend fun getRecommendations(): ResultWrapper<List<RecommendModel>>
}