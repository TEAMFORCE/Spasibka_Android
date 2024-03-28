package com.teamforce.thanksapp.presentation.viewmodel.challenge.challengeChain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.teamforce.thanksapp.domain.interactors.ChainInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ParticipantsChainViewModel @Inject constructor(
    private val chainInteractor: ChainInteractor
) : ViewModel() {

    fun loadParticipants(chainId: Long) = chainInteractor.loadChainParticipants(chainId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(viewModelScope).map {
        it.map { it }
    }
}