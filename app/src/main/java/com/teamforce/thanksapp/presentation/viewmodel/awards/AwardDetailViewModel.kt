package com.teamforce.thanksapp.presentation.viewmodel.awards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.awards.GainAwardResponse
import com.teamforce.thanksapp.domain.interactors.AwardsInteractor
import com.teamforce.thanksapp.domain.models.awards.AwardState
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AwardDetailViewModel @Inject constructor(
    private val awardsInteractor: AwardsInteractor
): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val _gainAward = MutableLiveData<GainAwardResponse>()
    val gainAward: LiveData<GainAwardResponse>
        get() = _gainAward

    private val _awardState = MutableLiveData<AwardState>()
    val awardState: LiveData<AwardState> = _awardState

    fun setAwardState(state: AwardState) {
        _awardState.value = state
    }

    fun gainAward(
        awardId: Long
    ){
        viewModelScope.launch {
            awardsInteractor.gainAward(awardId).toResultState(
                onSuccess = {
                    _gainAward.postValue(it)
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

    fun setInStatusAward(
        awardId: Long
    ){
        viewModelScope.launch {
            awardsInteractor.setInStatusAward(awardId).toResultState(
                onSuccess = {
                    setAwardState(AwardState.SET_IN_STATUS)
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