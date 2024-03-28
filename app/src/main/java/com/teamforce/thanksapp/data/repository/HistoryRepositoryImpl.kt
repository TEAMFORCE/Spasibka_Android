package com.teamforce.thanksapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.request.CancelTransactionRequest
import com.teamforce.thanksapp.data.response.CancelTransactionResponse
import com.teamforce.thanksapp.data.response.HistoryItem
import com.teamforce.thanksapp.data.sources.history.HistoryGroupPagingSource
import com.teamforce.thanksapp.data.sources.history.HistoryPagingSource
import com.teamforce.thanksapp.domain.mappers.history.HistoryMapper
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.domain.models.history.UserTransactionsGroupModel
import com.teamforce.thanksapp.domain.repositories.HistoryRepository
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.mapWrapperData
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val historyMapper: HistoryMapper
) : HistoryRepository {


    override fun getTransactionsGroup(): Flow<PagingData<UserTransactionsGroupModel.UserGroupModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                HistoryGroupPagingSource(
                    api = thanksApi,
                    mapper = historyMapper
                )
            }
        ).flow
    }


    override fun getHistory(
        receivedOnly: Int?,
        sentOnly: Int?,
        fromDate: String?,
        upDate: String?,
    ): Flow<PagingData<HistoryItemModel.UserTransactionsModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE_30,
                prefetchDistance = Consts.PREFETCH_DISTANCE,
                pageSize = Consts.PAGE_SIZE_30,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                HistoryPagingSource(
                    api = thanksApi,
                    receivedOnly = receivedOnly,
                    sentOnly = sentOnly,
                    mapper = historyMapper,
                    fromDate = fromDate,
                    upDate = upDate,
                )
            }
        ).flow
    }

    override  fun getLastThreeHistoryElement(): Flow<PagingData<HistoryItemModel.UserTransactionsModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                HistoryPagingSource(
                    api = thanksApi,
                    receivedOnly = null,
                    sentOnly = null,
                    mapper = historyMapper,
                    showOnlyThreeElement = true,
                    upDate = null,
                    fromDate = null,
                )
            }
        ).flow

    }

    override suspend fun cancelTransaction(
        id: String,
        status: CancelTransactionRequest
    ): ResultWrapper<CancelTransactionResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.cancelTransaction(id, status)
        }
    }

    override suspend fun getTransaction(id: Int): ResultWrapper<HistoryItemModel.UserTransactionsModel> {
        return safeApiCall(Dispatchers.IO){
             historyMapper.map(thanksApi.getTransactionByIdInHistory(id))
        }
    }

    companion object {
        const val PAGE_SIZE = 15
    }

}