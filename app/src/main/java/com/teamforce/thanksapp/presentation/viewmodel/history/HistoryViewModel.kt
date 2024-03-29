package com.teamforce.thanksapp.presentation.viewmodel.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.HistoryItem
import com.teamforce.thanksapp.data.response.LikeResponse
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.domain.repositories.HistoryRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _allData = MutableLiveData<HistoryItemModel.UserTransactionsModel?>()
    val allData: LiveData<HistoryItemModel.UserTransactionsModel?> = _allData

    fun getTransaction(
        transactionId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = historyRepository.getTransaction(transactionId)) {
                    is ResultWrapper.Success -> {
                        _allData.postValue(result.value)
                        Log.d("Token", " Результат лайка ${result.value}")
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                          //  _pressLikesError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            //_internetError.postValue(true)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

}
