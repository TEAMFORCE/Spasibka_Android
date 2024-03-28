package com.teamforce.thanksapp.domain.repositories

import androidx.paging.PagingData
import com.teamforce.thanksapp.data.entities.StatusResponse
import com.teamforce.thanksapp.data.entities.events.EventDataEntity
import com.teamforce.thanksapp.data.entities.events.EventFilterEntity
import com.teamforce.thanksapp.domain.models.events.EventDataModel
import com.teamforce.thanksapp.domain.models.events.EventFilterModel
import com.teamforce.thanksapp.domain.models.feed.FeedItemByIdModel
import com.teamforce.thanksapp.domain.models.feed.FeedModel
import com.teamforce.thanksapp.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface FeedRepository {

    fun getEvents(filters: List<Int>?): Flow<PagingData<EventDataModel>>

    suspend fun getFilters(): ResultWrapper<EventFilterModel>
    suspend fun saveFilters(filters: List<Int>?): ResultWrapper<StatusResponse>

    suspend fun getTransactionById(transactionId: Int): ResultWrapper<FeedItemByIdModel>
}