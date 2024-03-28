package com.teamforce.thanksapp.presentation.viewmodel.benefit.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.domain.interactors.reviews.ReviewsInteractor
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewDetailViewModel @Inject constructor(
    private val reviewsInteractor: ReviewsInteractor,
    private val userDataRepository: UserDataRepository
): ViewModel() {

    private val _viewState = MutableStateFlow(
        ReviewDetailViewState(
            error = null
        )
    )
    val viewState: StateFlow<ReviewDetailViewState> = _viewState

    fun getMyName() = userDataRepository.getName()
    fun getAvatar() = userDataRepository.getUserAvatar()

    fun obtainError(error: String? = null) {
        _viewState.value = _viewState.value.copy(error = error)
    }

    fun obtainDeleted() {
        _viewState.value = _viewState.value.copy(deleted = false)
    }

    fun deleteReview(
        marketId: Int,
        reviewId: Long,
    ) {
        viewModelScope.launch {
            reviewsInteractor.deleteReview(marketId, reviewId).toResultState(
                onSuccess = {
                    _viewState.value = _viewState.value.copy(deleted = true)
                },
                onError = { error, code ->
                    if(error?.contains("was null but response body type was declared as non-null") == true){
                        _viewState.value = _viewState.value.copy(deleted = true)
                    }else{
                        _viewState.value =
                            _viewState.value.copy(error = "${code} ${error}")
                    }
                },
                onLoading = {
                    _viewState.value = _viewState.value.copy(isLoading = it)
                }
            )
        }
    }

   data class ReviewDetailViewState(
        val deleted: Boolean = false,
        val isLoading: Boolean = false,
        val error: String?
    )
}