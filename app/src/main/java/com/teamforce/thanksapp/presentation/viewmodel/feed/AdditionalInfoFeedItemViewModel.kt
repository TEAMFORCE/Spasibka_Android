package com.teamforce.thanksapp.presentation.viewmodel.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.response.CancelTransactionResponse
import com.teamforce.thanksapp.domain.models.feed.FeedItemByIdModel
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.repositories.FeedRepository
import com.teamforce.thanksapp.domain.repositories.ReactionRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class AdditionalInfoFeedItemViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val feedRepository: FeedRepository,
    private val reactionRepository: ReactionRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _dataOfTransaction = MutableLiveData<FeedItemByIdModel?>()
    val dataOfTransaction: MutableLiveData<FeedItemByIdModel?> = _dataOfTransaction

    private val _error = MutableLiveData<String>()
    val error: MutableLiveData<String> = _error


    private val _pressLikesError = MutableLiveData<String>()
    val pressLikesError: LiveData<String> = _pressLikesError
    private val _isLoadingLikes = MutableLiveData<Boolean>()
    val isLoadingLikes: LiveData<Boolean> = _isLoadingLikes

    fun getProfileUserName() = userDataRepository.getUserName()


    fun loadTransactionDetail(
        transactionId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = feedRepository.getTransactionById(transactionId)) {
                    is ResultWrapper.Success -> {
                        _dataOfTransaction.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _error.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _error.postValue("Ошибка сети")
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }



    fun pressLike(
        transactionId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = reactionRepository.pressLike(transactionId, ObjectsToLike.TRANSACTION)) {
                    is ResultWrapper.Success -> {
                        Log.d("Token", " Результат лайка ${result.value}")
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _pressLikesError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _error.postValue("Ошибка сети")
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }
}