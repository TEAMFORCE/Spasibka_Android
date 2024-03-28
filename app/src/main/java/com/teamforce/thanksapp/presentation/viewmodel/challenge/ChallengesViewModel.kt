package com.teamforce.thanksapp.presentation.viewmodel.challenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.teamforce.thanksapp.domain.interactors.ChainInteractor
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val chainInteractor: ChainInteractor
): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _needUpdateList = MutableStateFlow(false)
    val needUpdateList: StateFlow<Boolean> = _needUpdateList


    fun setNeedUpdateListValue(value: Boolean) {
        _needUpdateList.value = value
    }


    val allChallenge = challengeRepository.loadChallenge(activeOnly = 0).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        ).cachedIn(viewModelScope).map {
            it.map { it }
        }

    val activeChallenge = challengeRepository.loadChallenge(activeOnly = 1).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(viewModelScope).map {
        it.map { it }
    }

    val delayedChallenge = challengeRepository.loadChallenge(showDelayedChallenges = 1).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(viewModelScope).map {
        it.map { it }
    }

    val challengeChains = chainInteractor.loadChains().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(viewModelScope).map {
        it.map { it }
    }

    fun dependentChallenges(challengeId: Int) = challengeRepository.loadDependentChallenges(challengeId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(viewModelScope).map {
        it.map { it }
    }

    fun challengesFromChain(chainId: Long) = chainInteractor.loadChallengesFromTheChain(chainId = chainId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(viewModelScope).map {
        it.map { it }
    }




}