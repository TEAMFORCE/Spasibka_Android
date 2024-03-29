package com.teamforce.thanksapp.domain.interactors.eventScreen

import androidx.paging.PagingData
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.StatusResponse
import com.teamforce.thanksapp.data.repository.FilterType
import com.teamforce.thanksapp.domain.models.events.EventDataModel
import com.teamforce.thanksapp.domain.models.events.EventFilterModel
import com.teamforce.thanksapp.domain.models.events.EventTypeModel
import com.teamforce.thanksapp.domain.repositories.FeedRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.mapWrapperData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventsInteractor @Inject constructor(
    private val feedRepository: FeedRepository,
) {
    fun getEvents(filter: Set<Int>?): Flow<PagingData<EventDataModel>> {
        val filterList = filter?.toList() ?: mutableListOf()
        return feedRepository.getEvents(filterList)
    }

    suspend fun getFilters(): ResultWrapper<EventFilterModel> {
        return feedRepository.getFilters()
    }

    suspend fun saveFilters(filter: Set<Int>?): ResultWrapper<StatusResponse> {
        val filterList = filter?.toList() ?: mutableListOf()
        return feedRepository.saveFilters(filterList)
    }
}

private fun ResultWrapper<EventFilterModel>.filterExceptPurchase(): ResultWrapper<EventFilterModel> =
    mapWrapperData { filterModel ->
        filterModel.copy(eventtypes = filterModel.eventtypes.filter { typeModel ->
            !typeModel.ids.contains(
                FilterType.PURCHASE.id
            )
        })
    }

