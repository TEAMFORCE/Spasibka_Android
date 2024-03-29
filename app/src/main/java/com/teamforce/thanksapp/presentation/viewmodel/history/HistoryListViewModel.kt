package com.teamforce.thanksapp.presentation.viewmodel.history

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.request.CancelTransactionRequest
import com.teamforce.thanksapp.domain.interactors.HistoryInteractor
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.domain.models.history.UserTransactionsGroupModel
import com.teamforce.thanksapp.utils.Result
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.getDateTimeLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HistoryListViewModel @Inject constructor(
    private val historyInteractor: HistoryInteractor,
    private val userDataRepository: com.teamforce.thanksapp.utils.UserDataRepository,
    application: Application
) : AndroidViewModel(application) {



    private val _cancellationResult: MutableLiveData<Result<Int>> =
        MutableLiveData<Result<Int>>()
    val cancellationResult: LiveData<Result<Int>> = _cancellationResult

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    fun cancelUserTransaction(id: Int) {
        viewModelScope.launch {

            when (historyInteractor.cancelTransaction(
                id.toString(),
                CancelTransactionRequest("D")
            )) {
                is ResultWrapper.GenericError -> {}
                is ResultWrapper.NetworkError -> {
                    _internetError.postValue(true)
                }
                is ResultWrapper.Success -> _cancellationResult.postValue(Result.Success(0))
            }
        }
    }

    fun getHistoryGroup(context: Context) = historyInteractor.getHistoryGroup().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(viewModelScope).map {
        it.map {
            it
        }
    }.map {
        it.insertSeparators<UserTransactionsGroupModel.UserGroupModel, UserTransactionsGroupModel>{ before, after ->
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            if (after == null) return@insertSeparators null
            if(before == null) {
                val afterTime: LocalDate = LocalDate.parse(after.date, formatter)
                val label = getDateTimeLabel(afterTime, context)
                return@insertSeparators UserTransactionsGroupModel.DateTimeSeparator(label)
            }
            val beforeTime: LocalDate =
                LocalDate.parse(
                    before.date,
                    formatter
                )

            val afterTime: LocalDate =
                LocalDate.parse(
                    after.date,
                    formatter
                )

            if (beforeTime.dayOfMonth != afterTime.dayOfMonth) {
                val title = getDateTimeLabel(afterTime, context)
                UserTransactionsGroupModel.DateTimeSeparator(title)
            } else null
        }
    }


    //todo исправить. вот так делать три поля с потоками не надо. Надо передать параметр. Исправить
    fun getAllHistory(context: Context) = historyInteractor.getHistory(
        receivedOnly = null,
        sentOnly = null,
        fromDate = _fromToDate.value,
        upDate = _upToDate.value
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(viewModelScope).map {
        it.map {
            it
        }
    }.map {
            it.insertSeparators<HistoryItemModel.UserTransactionsModel, HistoryItemModel> { before, after ->
                if (after == null) return@insertSeparators null


                if (before == null) {
                    val afterTime: LocalDate =
                        LocalDateTime.parse(
                            after.updatedAt.replace(
                                "Z",
                                ""
                            )
                        ).atZone(ZoneOffset.UTC).toLocalDate()

                    val label = getDateTimeLabel(afterTime, context)
                    return@insertSeparators HistoryItemModel.DateTimeSeparator(label)
                }

                val beforeTime: LocalDate =
                    LocalDateTime.parse(
                        before.updatedAt.replace(
                            "Z",
                            ""
                        )
                    ).atZone(ZoneOffset.UTC).toLocalDate()

                val afterTime: LocalDate =
                    LocalDateTime.parse(
                        after.updatedAt.replace(
                            "Z",
                            ""
                        )
                    ).atZone(ZoneOffset.UTC).toLocalDate()

                if (beforeTime.dayOfMonth != afterTime.dayOfMonth) {
                    val title = getDateTimeLabel(afterTime, context)
                    HistoryItemModel.DateTimeSeparator(title)
                } else null
            }
        }

    fun getReceived(context: Context) = historyInteractor.getHistory(
        receivedOnly = 1,
        sentOnly = null,
        fromDate = _fromToDate.value,
        upDate = _upToDate.value
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(viewModelScope).map {
        it.map {
            it
        }
    }
        .map {
            it.insertSeparators<HistoryItemModel.UserTransactionsModel, HistoryItemModel> { before, after ->
                if (after == null) return@insertSeparators null


                if (before == null) {
                    val afterTime: LocalDate =
                        LocalDateTime.parse(
                            after.updatedAt.replace(
                                "Z",
                                ""
                            )
                        ).atZone(ZoneOffset.UTC).toLocalDate()

                    val label = getDateTimeLabel(afterTime, context)
                    return@insertSeparators HistoryItemModel.DateTimeSeparator(label)
                }

                val beforeTime: LocalDate =
                    LocalDateTime.parse(
                        before.updatedAt.replace(
                            "Z",
                            ""
                        )
                    ).atZone(ZoneOffset.UTC).toLocalDate()

                val afterTime: LocalDate =
                    LocalDateTime.parse(
                        after.updatedAt.replace(
                            "Z",
                            ""
                        )
                    ).atZone(ZoneOffset.UTC).toLocalDate()

                if (beforeTime.dayOfMonth != afterTime.dayOfMonth) {
                    val title = getDateTimeLabel(afterTime, context)
                    HistoryItemModel.DateTimeSeparator(title)
                } else null
            }
        }

    fun getSent(context: Context) = historyInteractor.getHistory(
        receivedOnly = null,
        sentOnly = 1,
        fromDate = _fromToDate.value,
        upDate = _upToDate.value
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(viewModelScope).map {
        it.map {
            it
        }
    }
        .map {
            it.insertSeparators<HistoryItemModel.UserTransactionsModel, HistoryItemModel> { before, after ->
                if (after == null) return@insertSeparators null


                if (before == null) {
                    val afterTime: LocalDate =
                        LocalDateTime.parse(
                            after.updatedAt.replace(
                                "Z",
                                ""
                            )
                        ).atZone(ZoneOffset.UTC).toLocalDate()

                    val label = getDateTimeLabel(afterTime, context)
                    return@insertSeparators HistoryItemModel.DateTimeSeparator(label)
                }

                val beforeTime: LocalDateTime =
                    LocalDateTime.parse(
                        before.updatedAt.replace(
                            "Z",
                            ""
                        )
                    )

                val afterTime: LocalDate =
                    LocalDateTime.parse(
                        after.updatedAt.replace(
                            "Z",
                            ""
                        )
                    ).atZone(ZoneOffset.UTC).toLocalDate()

                if (beforeTime.dayOfMonth != afterTime.dayOfMonth) {
                    val title = getDateTimeLabel(afterTime, context)
                    HistoryItemModel.DateTimeSeparator(title)
                } else null
            }
        }


    private var _fromToDate = MutableLiveData<Long?>()
    val fromToDate = _fromToDate

    private var _upToDate = MutableLiveData<Long?>()
    val upToDate = _upToDate

    private var _filterHasBeenUpdated = MutableLiveData<Boolean>(false)
    val filterHasBeenUpdated = _filterHasBeenUpdated

    fun setFilterUpdated(boolean: Boolean){
        _filterHasBeenUpdated.postValue(boolean)
    }

    fun resetDate() {
        setFromToDateFilter()
        setUpToDateFilter()
    }

    fun setFromToDateFilter(fromTo: Long? = null) {
        _fromToDate.postValue(fromTo)
    }

    fun setUpToDateFilter(upTo: Long? = null) {
        _upToDate.postValue(upTo)
    }

    fun getUsername() = userDataRepository.getUserName()
    fun getUserId() = userDataRepository.getProfileId()?.toIntOrNull()

}