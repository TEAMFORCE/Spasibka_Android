package com.teamforce.thanksapp.presentation.viewmodel.benefit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.AddBenefitToCartResponse
import com.teamforce.thanksapp.data.response.LikeResponse
import com.teamforce.thanksapp.domain.interactors.reviews.ReviewsInteractor
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.BenefitModel
import com.teamforce.thanksapp.domain.models.benefit.ReviewModel
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.models.reactions.LikeResponseModel
import com.teamforce.thanksapp.domain.repositories.BenefitRepository
import com.teamforce.thanksapp.domain.repositories.ReactionRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class BenefitDetailViewModel @Inject constructor(
    private val benefitRepository: BenefitRepository,
    private val reactionRepository: ReactionRepository,
    private val reviewsInteractor: ReviewsInteractor
): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _offer = MutableLiveData<BenefitItemByIdModel?>()
    val offer: LiveData<BenefitItemByIdModel?> = _offer

    private val _offerError = MutableLiveData<String>()
    val offerError: LiveData<String> = _offerError

    private val _resultAddBenefitToCart = MutableLiveData<AddBenefitToCartResponse?>()
    val resultAddBenefitToCart: LiveData<AddBenefitToCartResponse?> = _resultAddBenefitToCart

    private val _addOfferError = MutableLiveData<String>()
    val addOfferError: LiveData<String> = _addOfferError

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    private var marketId: Int = -1
    private var offerId: Int = -1

    fun setOfferId(offerIdOuter: Int){
        offerId = offerIdOuter
    }

    fun getOfferId() = offerId

    fun setMarketId(marketIdOuter: Int){
        marketId = marketIdOuter
    }

    fun getMarketId() = marketId


    fun addOfferToCart(
        offerId: Int,
        quantity: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = benefitRepository.addBenefitToCart(offerId, marketId, quantity, -1, null)) {
                    is ResultWrapper.Success -> {
                        _resultAddBenefitToCart.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _addOfferError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun loadOffer(
        offerId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = benefitRepository.getOffersById(offerId, marketId)) {
                    is ResultWrapper.Success -> {
                        _offer.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            try {
                                val jArrayError = JSONArray(result)
                                _offerError.postValue(
                                    jArrayError.toString()
                                        .subSequence(2, jArrayError.toString().length - 2).toString()
                                )
                            }catch (e: java.lang.Exception){
                                _offerError.postValue("Неизвестная ошибка")
                            }
                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    private val _likeResult =  MutableLiveData<LikeResponseModel?>()
    val likeResult: LiveData<LikeResponseModel?> =  _likeResult
    private val _likeError = MutableLiveData<String>()
    val likeError: LiveData<String> = _likeError


    fun pressLike(
        offerId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = reactionRepository.pressLike(offerId, ObjectsToLike.OFFER)) {
                    is ResultWrapper.Success -> {
                        _likeResult.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _likeError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _likeError.postValue("Ошибка сети")
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

}