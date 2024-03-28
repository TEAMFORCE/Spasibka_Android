package com.teamforce.thanksapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.Stickers.StickerEntity
import com.teamforce.thanksapp.data.entities.transaction.SendCoinsSettingsEntity
import com.teamforce.thanksapp.data.response.BalanceResponse
import com.teamforce.thanksapp.data.response.SendCoinsResponse
import com.teamforce.thanksapp.data.response.UserListItem.UserBean
import com.teamforce.thanksapp.domain.repositories.ReactionRepository
import com.teamforce.thanksapp.domain.repositories.TransactionsRepository
import com.teamforce.thanksapp.domain.repositories.UsersRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val thanksApi: ThanksApi,
    val userDataRepository: UserDataRepository,
    private val reactionRepository: ReactionRepository,
    private val usersRepository: UsersRepository,
    private val transactionsRepository: TransactionsRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccessOperation = MutableLiveData<Boolean>()
    val isSuccessOperation: LiveData<Boolean> = _isSuccessOperation
    private val _sendCoinsError = MutableLiveData<String>()
    val sendCoinsError: LiveData<String> = _sendCoinsError
    private val _balance = MutableLiveData<BalanceResponse>()
    val balance: LiveData<BalanceResponse> = _balance
    private val _balanceError = MutableLiveData<String>()
    val balanceError: LiveData<String> = _balanceError


    private val _settingsTransaction = MutableLiveData<SendCoinsSettingsEntity>()
    val settingsTransaction: LiveData<SendCoinsSettingsEntity> = _settingsTransaction
    private val _settingsTransactionError = MutableLiveData<String>()
    val settingsTransactionError: LiveData<String> = _settingsTransactionError

    private val _internetError = MutableLiveData<String>()
    val internetError: LiveData<String> = _internetError

    fun clearSendCoinsError() = _sendCoinsError.postValue("")


    fun setSuccessOperationFalse() {
        _isSuccessOperation.postValue(false)
    }

    fun loadSendCoinsSettings() {
        viewModelScope.launch {
            transactionsRepository.getSendCoinsSettings().toResultState(
                onSuccess = {
                    _settingsTransaction.postValue(it)
                },
                onError = { error, code ->
                    _settingsTransactionError.postValue("$code $error")
                },
                onLoading = {

                }
            )
        }
    }

    private val _userBean = MutableLiveData<UserBean>()
    val userBean: LiveData<UserBean> = _userBean

    fun loadUserBean(userId: Int) {
        viewModelScope.launch {
            usersRepository.getUserBean(userId).toResultState(
                onSuccess = {
                    _userBean.postValue(it)
                },
                onError = { error, code ->
                },
                onLoading = {

                }
            )
        }
    }


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
                    } else {
                        _balanceError.postValue(response.message() + " " + response.code())
                    }
                }

                override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                    _isLoading.postValue(false)
                    _balanceError.postValue(t.message)
                    //_internetError.postValue(t.message)
                }
            })
        }
    }

    fun sendCoinsWithImage(
        recipient: Int, amount: Int,
        reason: String,
        isAnon: Boolean,
        isPublic: Boolean,
        imageFilePart: List<MultipartBody.Part?>?,
        listOfTagsCheckedValues: MutableList<Int>?,
        stickerId: Int?,
    ) {
        viewModelScope.launch {
            transactionsRepository.sendCoins(
                recipient,
                amount,
                reason,
                isAnon,
                isPublic,
                imageFilePart,
                listOfTagsCheckedValues,
                stickerId
            ).toResultState(
                onSuccess = {
                    _isSuccessOperation.postValue(true)
                },
                onError = { error, code ->
                    _sendCoinsError.postValue("$code $error")
                },
                onLoading = {

                }
            )
        }
    }

    private val currentQuery = MutableStateFlow("")

    fun setQuery(query: String) {
        currentQuery.value = query
    }


    val users: Flow<PagingData<UserBean>> = currentQuery.flatMapLatest {
        usersRepository.getUsersWithInput(it).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        ).cachedIn(viewModelScope)
    }


    private val _stickers = MutableLiveData<List<StickerEntity>?>()
    val stickers: MutableLiveData<List<StickerEntity>?> = _stickers
    private val _stickersError = MutableLiveData<String>()
    val stickersError: LiveData<String> = _stickersError

    private val _checkedSticker = MutableLiveData<CheckedSticker?>()
    val checkedSticker: MutableLiveData<CheckedSticker?> = _checkedSticker

    fun setCheckedStickerId(checkedStickerId: CheckedSticker) {
        _checkedSticker.postValue(checkedStickerId)
    }

    fun clearCheckedStickerId() {
        _checkedSticker.postValue(null)
    }


    fun loadAllStickers(
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = reactionRepository.getAllStickers()) {
                    is ResultWrapper.Success -> {
                        _stickers.postValue(result.value)
                        Log.d("Token", " Результат лайка ${result.value}")
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _stickersError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(result.toString())
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }
}

data class CheckedSticker(
    val id: Int,
    val image: String
)
