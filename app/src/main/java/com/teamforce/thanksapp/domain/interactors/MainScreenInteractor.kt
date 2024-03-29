package com.teamforce.thanksapp.domain.interactors

import androidx.paging.PagingData
import com.teamforce.thanksapp.domain.models.events.EventDataModel
import com.teamforce.thanksapp.domain.models.recommendations.RecommendModel
import com.teamforce.thanksapp.domain.repositories.FeedRepository
import com.teamforce.thanksapp.domain.repositories.RecommendationsRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainScreenInteractor @Inject constructor(
    private val feedRepository: FeedRepository,
    private val recommendationsRepository: RecommendationsRepository
) {
    fun getEvents(): Flow<PagingData<EventDataModel>> {
        return feedRepository.getEvents(null)
    }

    suspend fun getRecommends(): ResultWrapper<List<RecommendModel>> {
        return recommendationsRepository.getRecommendations()
    }
}