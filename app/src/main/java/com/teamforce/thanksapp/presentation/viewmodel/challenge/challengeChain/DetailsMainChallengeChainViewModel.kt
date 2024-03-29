package com.teamforce.thanksapp.presentation.viewmodel.challenge.challengeChain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.domain.interactors.ChainInteractor
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChainModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.StepModel
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryItem
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsMainChallengeChainViewModel @Inject constructor(
    private val chainInteractor: ChainInteractor
): ViewModel() {

    private val _chain = MutableLiveData<ChainModel>()
    val chain: LiveData<ChainModel> = _chain

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error


    fun loadChain(chainId: Long){
        viewModelScope.launch {
            chainInteractor.loadChain(chainId).toResultState(
                onSuccess = {
                    _chain.postValue(it)
                },
                onLoading = {},
                onError = { error, code ->
                    _error.postValue("$code $error")
                }
            )
        }
    }

    private val _steps = MutableLiveData<List<StepModel>>()
    val steps: LiveData<List<StepModel>> = _steps

    fun saveSteps(steps: List<StepModel>) = _steps.postValue(steps)
}