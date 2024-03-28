package com.teamforce.thanksapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.StatusResponse
import com.teamforce.thanksapp.data.entities.events.EventFilterEntity
import com.teamforce.thanksapp.data.sources.feed.EventPagingSource
import com.teamforce.thanksapp.domain.mappers.feed.FeedMapper
import com.teamforce.thanksapp.domain.models.events.EventDataModel
import com.teamforce.thanksapp.domain.models.events.EventFilterModel
import com.teamforce.thanksapp.domain.models.feed.FeedItemByIdModel
import com.teamforce.thanksapp.domain.models.feed.FeedModel
import com.teamforce.thanksapp.domain.repositories.FeedRepository
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val feedMapper: FeedMapper,
) : FeedRepository {


    override fun getEvents(filters: List<Int>?): Flow<PagingData<EventDataModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 5,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                EventPagingSource(
                    api = thanksApi,
                    feedMapper = feedMapper,
                    filters = filters
                )
            }
        ).flow
    }

    override suspend fun getFilters(): ResultWrapper<EventFilterModel> {
        return safeApiCall(Dispatchers.IO) {
            feedMapper.mapFilterList(
                thanksApi.getEventsNew(
                    limit = 0,
                    offset = 0,
                    filters = null
                ).filter
            )
        }
    }

    override suspend fun saveFilters(filters: List<Int>?): ResultWrapper<StatusResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.saveFilters(
                SaveFilterRequest(
                    data = filters
                )
            )
        }
    }

    override suspend fun getTransactionById(transactionId: Int): ResultWrapper<FeedItemByIdModel> {
        val result = safeApiCall(Dispatchers.IO) {
            feedMapper.mapEntityByIdToModel(
                thanksApi.getTransactionById(transactionId.toString())
            )
        }
        return result
    }

    data class SaveFilterRequest(
        val data: List<Int>?,
    )
}

private fun EventFilterEntity.filterExceptPurchase(): EventFilterEntity =
    copy(eventtypes = eventtypes.filter { it.id != FilterType.PURCHASE.id })

enum class FilterType(val id: Int) {
    PURCHASE(304)
}