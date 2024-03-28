package com.teamforce.thanksapp.presentation.viewmodel.mainScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.response.BalanceResponse
import com.teamforce.thanksapp.domain.interactors.MainScreenInteractor
import com.teamforce.thanksapp.domain.models.recommendations.RecommendModel
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val thanksApi: ThanksApi,
    private val mainScreenInteractor: MainScreenInteractor
): ViewModel() {

    private val _internetError = MutableLiveData<String>()
    val internetError: LiveData<String> = _internetError
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _balance = MutableLiveData<BalanceResponse>()
    val balance: LiveData<BalanceResponse> = _balance



    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val coroutineScope =
        CoroutineScope(CoroutineExceptionHandler { _, exception ->
            _error.postValue(exception.localizedMessage)
            Timber.e(exception)
        } + Dispatchers.IO)


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
                        _error.postValue(response.message() + " " + response.code())
                    }
                }

                override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                    when (t) {
                        is IOException -> {
                            _internetError.postValue(t.message)
                        }
                    }
                    _isLoading.postValue(false)
                    _error.postValue(t.message)
                }
            })
        }
    }

    fun getEvents() = mainScreenInteractor.getEvents().stateIn(
        scope =  coroutineScope,
        started = SharingStarted.WhileSubscribed(5000, replayExpirationMillis = 1000),
        initialValue = PagingData.empty(),
    ).cachedIn(coroutineScope)

    private val _recommends = MutableLiveData<List<RecommendModel>>()
    val recommends: LiveData<List<RecommendModel>> = _recommends

    fun loadRecommends() {
        viewModelScope.launch {
            mainScreenInteractor.getRecommends()
                .toResultState(
                    onSuccess = {
                       _recommends.postValue(it)
                    },
                    onError = { error, code ->
                        _error.postValue("$code $error")

                    },
                    onLoading = {
                        _isLoading.postValue(it)
                    }
                )
        }
    }

}