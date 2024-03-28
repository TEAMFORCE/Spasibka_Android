package com.teamforce.thanksapp.presentation.viewmodel.benefit.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.api.ImageFileData
import com.teamforce.thanksapp.domain.interactors.reviews.ReviewsInteractor
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.RatingModel
import com.teamforce.thanksapp.domain.models.benefit.ReviewModel
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject


@HiltViewModel
class CreateReviewViewModel @Inject constructor(
    private val reviewsInteractor: ReviewsInteractor,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    // TODO Возможно стоит добавить полный сброс стейта

    private val _viewState = MutableStateFlow(
        CreateReviewViewState(
            review = null,
            error = null
        )
    )
    val viewState: StateFlow<CreateReviewViewState> = _viewState


    fun amIAdmin() = userDataRepository.userIsAdmin()
    fun getProfileId(): Int {
        val id = userDataRepository.getProfileId()
        if (!id.isNullOrEmpty()) {
            return id.toInt()
        }
        return -1
    }

    fun obtainError(error: String? = null) {
        _viewState.value = _viewState.value.copy(error = error)
    }

    fun obtainCreated() {
        _viewState.value = _viewState.value.copy(created = false)
    }

    fun obtainEdited() {
        _viewState.value = _viewState.value.copy(edited = false)
    }

    fun obtainReview(review: RatingModel.Review?) {
        _viewState.value = _viewState.value.copy(review = review)
    }

    fun createReview(
        marketId: Int,
        offerId: Long,
        imagesFilePart: List<MultipartBody.Part?>?,
        text: String,
        rate: Int
    ) {
        viewModelScope.launch {
            reviewsInteractor.sendReview(marketId, offerId, imagesFilePart, text, rate)
                .toResultState(
                    onSuccess = {
                        _viewState.value = _viewState.value.copy(created = true)
                    },
                    onError = { error, code ->
                        _viewState.value =
                            _viewState.value.copy(error = "${code} ${error}")
                    },
                    onLoading = {
                        _viewState.value = _viewState.value.copy(isLoading = it)
                    }
                )
        }
    }

    fun updateReview(
        marketId: Int,
        reviewId: Long,
        imagesFilePart: List<MultipartBody.Part?>?,
        fileList: List<ImageFileData>?,
        text: String,
        rate: Int
    ) {
        viewModelScope.launch {
            reviewsInteractor.updateReview(marketId, reviewId, imagesFilePart, fileList, text, rate)
                .toResultState(
                    onSuccess = {
                        _viewState.value = _viewState.value.copy(edited = true)
                    },
                    onError = { error, code ->
                        _viewState.value =
                            _viewState.value.copy(error = "${code} ${error}")
                    },
                    onLoading = {
                        _viewState.value = _viewState.value.copy(isLoading = it)
                    }
                )
        }
    }


}

data class CreateReviewViewState(
    val review: RatingModel.Review?,
    val created: Boolean = false,
    val edited: Boolean = false,
    val isLoading: Boolean = false,
    val error: String?
)