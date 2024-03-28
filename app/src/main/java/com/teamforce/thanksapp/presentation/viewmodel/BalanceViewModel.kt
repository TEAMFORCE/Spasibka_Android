package com.teamforce.thanksapp.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.request.CancelTransactionRequest
import com.teamforce.thanksapp.data.response.BalanceResponse
import com.teamforce.thanksapp.data.response.UserListItem
import com.teamforce.thanksapp.domain.interactors.HistoryInteractor
import com.teamforce.thanksapp.domain.mappers.history.HistoryMapper
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.domain.repositories.TransactionsRepository
import com.teamforce.thanksapp.domain.repositories.UsersRepository
import com.teamforce.thanksapp.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class BalanceViewModel @Inject constructor(
    private val thanksApi: ThanksApi,
    val userDataRepository: UserDataRepository,
    private val historyInteractor: HistoryInteractor,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    val userAvatar = userDataRepository.getUserAvatar()


    private val _internetError = MutableLiveData<String>()
    val internetError: LiveData<String> = _internetError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _balance = MutableLiveData<BalanceResponse>()
    val balance: LiveData<BalanceResponse> = _balance
    private val _balanceError = MutableLiveData<String>()
    val balanceError: LiveData<String> = _balanceError


    fun loadUserBalance() {
        _isLoading.postValue(true)
        viewModelScope.launch { callBalanceEndpoint(Dispatchers.Default) }
    }

    private suspend fun callBalanceEndpoint(
        coroutineDispatcher: CoroutineDispatcher
    ) {
        withContext(coroutineDispatcher) {
            thanksApi.getBalance().enqueue(object : Callback<BalanceResponse> {
                override fun onResponse(
                    call: Call<BalanceResponse>,
                    response: Response<BalanceResponse>
                ) {
                    _isLoading.postValue(false)
                    if (response.code() == 200) {
                        _balance.postValue(response.body())
                        Log.d("Token", "Пользовательские данные ${response.body()}")
                    } else {
                        _balanceError.postValue(response.message() + " " + response.code())
                    }
                }

                override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                    when (t) {
                        is IOException -> {
                            _internetError.postValue(t.message)
                        }
                    }
                    _isLoading.postValue(false)
                    _balanceError.postValue(t.message)
                }
            })
        }
    }

    fun getUsername() = userDataRepository.getUserName()
    fun getUserId() = userDataRepository.getProfileId()?.toIntOrNull()

    //todo исправить. вот так делать три поля с потоками не надо. Надо передать параметр. Исправить
    fun getAllHistory(context: Context) = historyInteractor.getLastThreeHistoryElement().stateIn(
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

    private val _cancellationResult: MutableLiveData<Result<Int>> =
        MutableLiveData<Result<Int>>()
    val cancellationResult: LiveData<Result<Int>> = _cancellationResult


    fun cancelUserTransaction(id: Int) {
        viewModelScope.launch {
            historyInteractor.cancelTransaction(
                id.toString(),
                CancelTransactionRequest("D")
            ).toResultState(
                onSuccess = {
                    _cancellationResult.postValue(Result.Success(0))
                },
                onLoading = {
                    _isLoading.postValue(it)
                },
                onError = { error, code ->
                    _internetError.postValue("$error $code")
                }
            )

        }

    }

    private val _users = MutableLiveData<List<UserListItem>>()
    val users: LiveData<List<UserListItem>> = _users

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getUsers() {
        viewModelScope.launch {
            usersRepository.getUsersWithoutPaging().toResultState(
                onSuccess = {
                    _users.postValue(it)
                },
                onLoading = {
                    _isLoading.postValue(it)
                },
                onError = { error, code ->
                    _error.postValue("$error $code")
                }
            )

        }
    }


}

