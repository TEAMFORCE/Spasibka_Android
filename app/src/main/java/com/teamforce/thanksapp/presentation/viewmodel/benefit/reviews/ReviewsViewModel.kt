package com.teamforce.thanksapp.presentation.viewmodel.benefit.reviews

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.teamforce.thanksapp.data.response.LikeResponse
import com.teamforce.thanksapp.domain.interactors.reviews.ReviewsInteractor
import com.teamforce.thanksapp.domain.interactors.reviews.ReviewsSortOption
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.RatingModel
import com.teamforce.thanksapp.domain.models.benefit.ReviewModel
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.models.reactions.LikeResponseModel
import com.teamforce.thanksapp.domain.repositories.ReactionRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.SingleLiveEvent
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val reviewsInteractor: ReviewsInteractor,
    private val userDataRepository: UserDataRepository,
    private val reactionRepository: ReactionRepository,
) : ViewModel() {

    var marketId: Int = 0
        private set

    var offerId: Int = 0
        private set

    fun setMarketId(id: Int) {
        marketId = id
    }

    fun setOfferId(id: Int) {
        offerId = id
    }

    fun amIAdmin() = userDataRepository.userIsAdmin()
    fun getProfileId(): Int {
        val id = userDataRepository.getProfileId()
        if (!id.isNullOrEmpty()) {
            return id.toInt()
        }
        return -1
    }


    fun getReviews(): Flow<PagingData<ReviewModel>> {
        return reviewsInteractor.getReviews(
            marketId = marketId,
            offerId = offerId.toLong(),
            sortOption = ReviewsSortOption.FROM_NEW_TO_OLD
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        ).cachedIn(viewModelScope)

    }

    private val _likeResult =  SingleLiveEvent<LikeResponseModel?>()
    val likeResult: SingleLiveEvent<LikeResponseModel?> =  _likeResult
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading



    fun pressLike(
        reviewId: Long,
        position: Int
    ) {
        viewModelScope.launch {
            reactionRepository.pressLike(reviewId.toInt(), ObjectsToLike.OFFER_REVIEW, position)?.toResultState(
                    onSuccess = {
                        _likeResult.postValue(it)
                    },
                    onError = { error, code ->
                        _error.postValue("$error $code")
                    },
                    onLoading = {

                    }
                )
        }
    }

    private val _ratingModel = MutableLiveData<RatingModel>()
    val ratingModel: LiveData<RatingModel> = _ratingModel

    fun getRating() {
        viewModelScope.launch {
            reviewsInteractor.getReview(marketId, offerId).toResultState(
                onSuccess = {
                    _ratingModel.postValue(it)
                },
                onError = { error, code ->
                    _error.postValue("$error $code")
                },
                onLoading = {
                    _isLoading.postValue(it)
                }
            )
        }
    }

    private val _deletedPosition = SingleLiveEvent<Int>()
    val deletedPosition: SingleLiveEvent<Int> = _deletedPosition

    fun deleteReview(
        reviewId: Long,
        position: Int
    ) {
        viewModelScope.launch {
            reviewsInteractor.deleteReview(marketId, reviewId).toResultState(
                onSuccess = {
                    _deletedPosition.postValue(position)
                },
                onError = { error, code ->
                    if(error?.contains("was null but response body type was declared as non-null") == true){
                        _deletedPosition.postValue(position)
                    }else{
                        _error.postValue("$error $code")
                    }
                },
                onLoading = {
                }
            )
        }
    }
}