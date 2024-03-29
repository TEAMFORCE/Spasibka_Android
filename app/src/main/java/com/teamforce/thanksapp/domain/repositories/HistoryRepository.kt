package com.teamforce.thanksapp.domain.repositories

import androidx.paging.PagingData
import com.teamforce.thanksapp.data.request.CancelTransactionRequest
import com.teamforce.thanksapp.data.response.CancelTransactionResponse
import com.teamforce.thanksapp.data.response.HistoryItem
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.domain.models.history.UserTransactionsGroupModel
import com.teamforce.thanksapp.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(
        receivedOnly: Int?,
        sentOnly: Int?,
        fromDate: String?,
        upDate: String?,
    ): Flow<PagingData<HistoryItemModel.UserTransactionsModel>>
    fun getLastThreeHistoryElement(): Flow<PagingData<HistoryItemModel.UserTransactionsModel>>

    fun getTransactionsGroup(): Flow<PagingData<UserTransactionsGroupModel.UserGroupModel>>

    suspend fun cancelTransaction(
        id: String,
        status: CancelTransactionRequest
    ): ResultWrapper<CancelTransactionResponse>

    suspend fun getTransaction(
        id: Int,
    ): ResultWrapper<HistoryItemModel.UserTransactionsModel>
}