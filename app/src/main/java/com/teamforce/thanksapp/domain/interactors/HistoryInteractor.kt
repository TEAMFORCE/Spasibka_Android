package com.teamforce.thanksapp.domain.interactors

import android.util.Log
import androidx.paging.PagingData
import com.teamforce.thanksapp.data.request.CancelTransactionRequest
import com.teamforce.thanksapp.data.response.CancelTransactionResponse
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.domain.models.history.UserTransactionsGroupModel
import com.teamforce.thanksapp.domain.repositories.HistoryRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.convertLongToUtcEndDateString
import com.teamforce.thanksapp.utils.convertLongToUtcStartDateString
import com.teamforce.thanksapp.utils.convertToUtcFormat
import com.teamforce.thanksapp.utils.mapWrapperData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HistoryInteractor @Inject constructor(
    private val historyRepository: HistoryRepository
) {

    fun getHistoryGroup(): Flow<PagingData<UserTransactionsGroupModel.UserGroupModel>>{
        return historyRepository.getTransactionsGroup()
    }

    fun getHistory(
        receivedOnly: Int?,
        sentOnly: Int?,
        fromDate: Long? = null,
        upDate: Long? = null
    ): Flow<PagingData<HistoryItemModel.UserTransactionsModel>> {
        Log.e("History", "FromDateBefore: ${fromDate}")
        Log.e("History", "UpDateBefore: ${upDate}")
        val _fromDate = convertToUtcFormat(fromDate, isMidnight = true)
        val _upDate = convertToUtcFormat(upDate, isMidnight = false)
        Log.e("History", "FromDateAfter: ${_fromDate}")
        Log.e("History", "UpDateAfter: ${_upDate}")
        return historyRepository.getHistory(
            receivedOnly, sentOnly,
            fromDate = _fromDate,
            upDate = _upDate
        )
    }

    fun getLastThreeHistoryElement(): Flow<PagingData<HistoryItemModel.UserTransactionsModel>> {
        return historyRepository.getLastThreeHistoryElement()
    }

    suspend fun cancelTransaction(
        id: String,
        status: CancelTransactionRequest
    ): ResultWrapper<CancelTransactionResponse> {
        return historyRepository.cancelTransaction(
            id, status
        ).mapWrapperData { it }
    }

    suspend fun getTransaction(id: Int): ResultWrapper<HistoryItemModel.UserTransactionsModel> {
        return historyRepository.getTransaction(id)
    }
}