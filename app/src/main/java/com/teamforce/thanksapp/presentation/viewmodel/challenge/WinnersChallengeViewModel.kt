package com.teamforce.thanksapp.presentation.viewmodel.challenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.GetChallengeWinnersResponse
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
import javax.inject.Inject

@HiltViewModel
class WinnersChallengeViewModel @Inject constructor(
   private val challengeRepository: ChallengeRepository,
   private val reactionRepository: ReactionRepository

): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _winners = MutableLiveData<List<GetChallengeWinnersResponse.Winner>?>()
    val winners: LiveData<List<GetChallengeWinnersResponse.Winner>?> = _winners
    private val _winnersError = MutableLiveData<String>()
    val winnersError: LiveData<String> = _winnersError

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError




    fun loadWinners(
        challengeId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = challengeRepository.loadWinners(challengeId)) {
                    is ResultWrapper.Success -> {
                        _winners.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _winnersError.postValue(result.error + " " + result.code)

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
        reportId: Int,
        position: Int
    ) {
        viewModelScope.launch {
            reactionRepository.pressLike(reportId, ObjectsToLike.REPORT_OF_CHALLENGE, position)?.toResultState(
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
}