package com.teamforce.thanksapp.presentation.viewmodel.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.GetChallengeResultResponse
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.models.reactions.LikeResponseModel
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.domain.repositories.ReactionRepository
import com.teamforce.thanksapp.model.domain.ChallengeModelById
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsMainChallengeViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val challengeRepository: ChallengeRepository,
    private val reactionRepository: ReactionRepository,
) : ViewModel() {

    fun clearChallengeData() {
        _viewState.value = _viewState.value.copy(challenge = null)
    }

    fun amIAdmin() = userDataRepository.userIsAdmin()

    fun obtainError(error: ChallengeDetailScreenState.Error? = null) {
        _viewState.value = _viewState.value.copy(error = error)
    }

    fun obtainChallengeNotFound(boolean: Boolean = false) {
        _viewState.value = _viewState.value.copy(challengeNotFound = boolean)
    }


    fun loadChallenge(
        challengeId: Int
    ) {
        viewModelScope.launch {
            challengeRepository.getChallengeById(challengeId).toResultState(
                onSuccess = {
                    _viewState.value = _viewState.value.copy(challenge = it)
                    _viewState.value = _viewState.value.copy(
                        likeResult = LikeResponseModel(
                            position = null,
                            isLiked = it.userLiked,
                            userId = it.creatorId,
                            likesAmount = it.likesAmount
                        )
                    )
                },
                onError = { error, code ->
                    if(code != "404"){
                        _viewState.value =
                            _viewState.value.copy(error = ChallengeDetailScreenState.Error(code?.toIntOrNull(), status = error))
                    }
                    _viewState.value =
                        _viewState.value.copy(challengeNotFound = code == "404")

                },
                onLoading = {
                    _viewState.value = _viewState.value.copy(isLoading = it)

                }
            )
        }
    }


    fun loadChallengeResult(
        challengeId: Int
    ) {
        viewModelScope.launch {
            challengeRepository.loadChallengeResult(challengeId).toResultState(
                onSuccess = {
                    _viewState.value = _viewState.value.copy(myResult = it)
                },
                onError = { error, code ->

                },
                onLoading = {
                }
            )
        }
    }


    fun getProfileId(): Int {
        val id = userDataRepository.getProfileId()
        if (!id.isNullOrEmpty()) {
            return id.toInt()
        }
        return -1
    }


    fun pressLike(
        challengeId: Int
    ) {
        viewModelScope.launch {
            reactionRepository.pressLike(challengeId, ObjectsToLike.CHALLENGE)?.toResultState(
                onSuccess = {
                    _viewState.value = _viewState.value.copy(likeResult = it)
                },
                onError = { error, code ->
                    _viewState.value =
                        _viewState.value.copy(error = ChallengeDetailScreenState.Error(code?.toIntOrNull(), status = error))
                },
                onLoading = {

                }
            )
        }
    }

    private val _viewState = MutableStateFlow(
        ChallengeDetailScreenState(myResult = null, challenge = null, likeResult = null)
    )

    val viewState: StateFlow<ChallengeDetailScreenState>
        get() = _viewState.asStateFlow()


    data class ChallengeDetailScreenState(
        val isLoading: Boolean = true,
        val error: Error? = null,
        val myResult: List<GetChallengeResultResponse>?,
        val challenge: ChallengeModelById?,
        val likeResult: LikeResponseModel?,
        val challengeNotFound: Boolean = false,
    ){
        data class Error(val code: Int?, val status: String?)
    }

}