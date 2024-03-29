package com.teamforce.thanksapp.presentation.viewmodel.challenge.challengeContainer

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.teamforce.thanksapp.domain.interactors.ChainInteractor
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChallengeChainsModel
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.utils.screenState.OneTimeEvent
import com.teamforce.thanksapp.utils.screenState.toOneTimeEvent
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ChallengeContainerViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val chainInteractor: ChainInteractor
) : ViewModel() {

    private val coroutineScope =
        CoroutineScope(CoroutineExceptionHandler { _, exception ->
            Timber.e(exception)
            _screenState.value =
                screenState.value.copy(error = exception.localizedMessage?.toOneTimeEvent())
        } + Dispatchers.IO)


    private val _screenState = MutableStateFlow(
        ChallengeContainerScreenState()
    )

    val screenState: StateFlow<ChallengeContainerScreenState>
        get() = _screenState.asStateFlow()



    private fun updateScreenState(
        isLoading: Boolean,
        error: String?,
    ): ChallengeContainerScreenState {
        return ChallengeContainerScreenState(
            isLoading = isLoading,
            error = error?.toOneTimeEvent()
        )
    }



    val challenges = {
       _screenState.value = _screenState.value.copy(isLoading = true)
        challengeRepository.loadChallengeOnlyFirstPage(activeOnly = 1).stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        )
            .onCompletion {  _screenState.value = _screenState.value.copy(isLoading = false) }
            .cachedIn(coroutineScope)
            .map {
                it.map { it }
            }
    }


    fun loadChainsOnlyFirstPage() {
        coroutineScope.launch {
            chainInteractor.loadChainsOnlyFirstPage().toResultState(
                onSuccess = {
                    _screenState.value = _screenState.value.copy(chains = it)
                },
                onLoading = {
                    _screenState.value = _screenState.value.copy(isLoading = it)
                },
                onError = { error, code ->
                    _screenState.value = _screenState.value.copy(error = "$error $code".toOneTimeEvent())
                }
            )
        }
    }

    /*
    fun getUsers() {
        viewModelScope.launch {
            usersRepository.getUsersWithoutPaging().toResultState(
                onSuccess = {
                    _users.postValue(it)
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
     */


    data class ChallengeContainerScreenState(
        val isLoading: Boolean = false,
        val error: OneTimeEvent<String>? = null,
        val chains: List<ChallengeChainsModel> = listOf()
    )


}