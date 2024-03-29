package com.teamforce.thanksapp.presentation.viewmodel.challenge

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.GetChallengeContendersResponse
import com.teamforce.thanksapp.data.response.LikeResponse
import com.teamforce.thanksapp.domain.models.challenge.ContenderModel
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.models.reactions.LikeResponseModel
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.domain.repositories.ReactionRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.SingleLiveEvent
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.FieldPosition
import javax.inject.Inject

@HiltViewModel
class ContendersChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val reactionRepository: ReactionRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccessOperation = MutableLiveData<SuccessResultCheckReport>()
    val isSuccessOperation: LiveData<SuccessResultCheckReport> = _isSuccessOperation

    private val _contenders = MutableLiveData<List<ContenderModel>?>()
    val contenders: LiveData<List<ContenderModel>?> = _contenders
    private val _contendersError = MutableLiveData<String>()
    val contendersError: LiveData<String> = _contendersError

    private val _checkReportError = MutableLiveData<String>()
    val checkReportError: LiveData<String> = _checkReportError

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError


    fun loadContenders(
        challengeId: Int,
        currentReportId: Int?
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = challengeRepository.loadContenders(challengeId, currentReportId)) {
                    is ResultWrapper.Success -> {
                        _contenders.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _contendersError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun checkReport(
        reportId: Int,
        state: Char,
        reasonOfReject: String?,
        challengeId: Int
    ) {
        val stateMap = mapOf<String, Char>("state" to state)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                _isSuccessOperation.postValue(SuccessResultCheckReport(state, false))
                when (val result = challengeRepository.checkChallengeReport(reportId, stateMap, reasonOfReject)) {
                    is ResultWrapper.Success -> {
                        _isSuccessOperation.postValue(SuccessResultCheckReport(state, true))
                        loadContenders(challengeId, null)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _checkReportError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    private val _likeResult =  SingleLiveEvent<LikeResponseModel?>()
    val likeResult: SingleLiveEvent<LikeResponseModel?> =  _likeResult
    private val _likeError = MutableLiveData<String>()
    val likeError: LiveData<String> = _likeError



    fun pressLike(
        offerId: Int,
        position: Int
    ) {
        viewModelScope.launch {
            reactionRepository.pressLike(offerId, ObjectsToLike.REPORT_OF_CHALLENGE, position)?.toResultState(
                onSuccess = {
                    _likeResult.postValue(it)
                },
                onError = { error, code ->
                    _likeError.postValue("$error $code")
                },
                onLoading = {

                }
            )
        }
    }

     data class SuccessResultCheckReport(
         val state: Char,
         val successResult: Boolean
     )
}