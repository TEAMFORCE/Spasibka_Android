package com.teamforce.thanksapp.presentation.viewmodel.challenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.CreateReportResponse
import com.teamforce.thanksapp.data.response.GetChallengeResultResponse
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class MyResultChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _myResult = MutableLiveData<List<GetChallengeResultResponse>?>()
    val myResult: LiveData<List<GetChallengeResultResponse>?> = _myResult
    private val _myResultError = MutableLiveData<String>()
    val myResultError: LiveData<String> = _myResultError

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError



    fun loadChallengeResult(
        challengeId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = challengeRepository.loadChallengeResult(challengeId)) {
                    is ResultWrapper.Success -> {
                        _myResult.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _myResultError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)

                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }


    private val _createReport = SingleLiveEvent<Boolean>()
    val createReport: SingleLiveEvent<Boolean> = _createReport

    private val _createReportError = SingleLiveEvent<String>()
    val createReportError: SingleLiveEvent<String> = _createReportError



    fun createReport(
        challengeId: Int,
        comment: String,
        image: MultipartBody.Part?
    ) {
        val challengeIdRequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), challengeId.toString())
        val commentRequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), comment)
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = challengeRepository.createReport(
                    image,
                    challengeIdRequestBody,
                    commentRequestBody)) {
                    is ResultWrapper.Success -> {
                        _createReport.postValue(true)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _createReportError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)

                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }
}