package com.teamforce.thanksapp.presentation.viewmodel.challenge.challengeChain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.domain.interactors.ChainInteractor
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.StepModel
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class QuestStepsListViewModel @Inject constructor(
    private val chainInteractor: ChainInteractor
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _steps = MutableLiveData<List<StepModel>>()
    val steps: LiveData<List<StepModel>> = _steps

    fun saveSteps(steps: List<StepModel>) = _steps.postValue(steps)

    fun loadSteps(chainId: Long) {
        viewModelScope.launch {
            chainInteractor.loadSteps(chainId).toResultState(
                onSuccess = {
                    _steps.postValue(it)
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