package com.teamforce.thanksapp.presentation.viewmodel.benefit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.teamforce.thanksapp.data.entities.benefit.OfferInCart
import com.teamforce.thanksapp.data.entities.benefit.Order
import com.teamforce.thanksapp.data.response.AddBenefitToCartResponse
import com.teamforce.thanksapp.data.response.TransactionOffersFromCartToOrderResponse
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.repositories.BenefitRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.FieldPosition
import javax.inject.Inject

@HiltViewModel
class ShoppingCartViewModel @Inject constructor(
    private val benefitRepository: BenefitRepository
) : ViewModel() {

    private val _state = MutableLiveData<StateShoppingCart>()
    val state: LiveData<StateShoppingCart> = _state

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

//    private val _offer = MutableLiveData<List<OfferInCart>?>()
//    val offer: MutableLiveData<List<OfferInCart>?> = _offer

    private val _offerError = MutableLiveData<String>()
    val offerError: LiveData<String> = _offerError

    private val _refuseOffer = MutableLiveData<Boolean>()
    val refuseOffer: LiveData<Boolean> = _refuseOffer

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError


    private val _resultUpdateBenefitToCart = MutableLiveData<AddBenefitToCartResponse?>()
    val resultUpdateBenefitToCart: LiveData<AddBenefitToCartResponse?> = _resultUpdateBenefitToCart

    private val _updateOfferError = MutableLiveData<String>()
    val updateOfferError: LiveData<String> = _updateOfferError

    private var marketId: Int = -1

    fun setMarketId(marketIdOuter: Int) {
        marketId = marketIdOuter
    }

    fun getMarketId() = marketId


    fun updateDataOfOffer(
        offerId: Int,
        quantity: Int,
        position: Int,
        isCheckedStatus: String
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = benefitRepository.addBenefitToCart(
                    offerId,
                    marketId,
                    quantity,
                    position,
                    isCheckedStatus
                )) {
                    is ResultWrapper.Success -> {
                        _resultUpdateBenefitToCart.postValue(result.value)
                        _state.postValue(StateShoppingCart.GOOD)
                    }

                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _updateOfferError.postValue(result.error + " " + result.code)
                            _state.postValue(StateShoppingCart.ERROR)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                            _state.postValue(StateShoppingCart.ERROR)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

//    fun loadOffer() {
//        _isLoading.postValue(true)
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                _isLoading.postValue(true)
//                _state.postValue(StateShoppingCart.LOADING)
//                when (val result = benefitRepository.loadOffersInCart()) {
//                    is ResultWrapper.Success -> {
//                        _offer.postValue(result.value)
//                        if(result.value.isEmpty()) _state.postValue(StateShoppingCart.NOTHING)
//                        else  _state.postValue(StateShoppingCart.GOOD)
//                    }
//                    else -> {
//                        if (result is ResultWrapper.GenericError) {
//                            _offerError.postValue(result.error + " " + result.code)
//                            _state.postValue(StateShoppingCart.ERROR)
//
//                        } else if (result is ResultWrapper.NetworkError) {
//                            _internetError.postValue(true)
//                            _state.postValue(StateShoppingCart.ERROR)
//                        }
//                    }
//                }
//                _isLoading.postValue(false)
//            }
//        }
//    }

    fun loadOffers(): Flow<PagingData<OfferInCart>> {
        return benefitRepository.loadOffersInCart(marketId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        ).cachedIn(viewModelScope)
    }

    private val _totalPrice = MutableLiveData(0)
    val totalPrice = _totalPrice

    fun loadTotalPrice(){
        viewModelScope.launch {
            benefitRepository.loadTotalPriceInCart(marketId).toResultState(
                onSuccess = {
                    _totalPrice.postValue(it)
                },
                onError = { error, code ->

                },
                onLoading = {

                }
            )
        }
    }


    //TODO результат запроса кидает в NetworkError, хотя запрос успешный
    fun refuseOffer(
        orderId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result =
                    benefitRepository.deleteOfferInCart(orderId = orderId, marketId)) {
                    is ResultWrapper.Success -> {
                        _refuseOffer.postValue(true)
                        loadTotalPrice()
                        _state.postValue(StateShoppingCart.GOOD)
                    }

                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _offerError.postValue(result.error + " " + result.code)
                            _refuseOffer.postValue(false)
                            // _state.postValue(StateShoppingCart.ERROR)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                            _refuseOffer.postValue(false)
                            // _state.postValue(StateShoppingCart.ERROR)
                        }
                    }
                }
                _isLoading.postValue(false)
                _refuseOffer.postValue(false)
            }
        }
    }

    private val _moveOffersToOrder = MutableLiveData<TransactionOffersFromCartToOrderResponse?>()
    val moveOffersToOrder: MutableLiveData<TransactionOffersFromCartToOrderResponse?> =
        _moveOffersToOrder

    private val _moveOffersToOrderError = MutableLiveData<Boolean>()
    val moveOffersToOrderError: LiveData<Boolean> = _moveOffersToOrderError

    fun transactionOffersFromCartToOrder(
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                _state.postValue(StateShoppingCart.LOADING)
                when (val result =
                    benefitRepository.transactionOffersFromCartToOrder(marketId = marketId)) {
                    is ResultWrapper.Success -> {
                        _moveOffersToOrder.postValue(result.value)
                        _moveOffersToOrderError.postValue(false)
                        _state.postValue(StateShoppingCart.ORDER_SUCCESS)
                    }

                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _moveOffersToOrderError.postValue(true)
                            _state.postValue(StateShoppingCart.GOOD)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                            _moveOffersToOrderError.postValue(true)
                            _state.postValue(StateShoppingCart.ERROR)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

}

enum class IsCheckedStatus(val definition: String) {
    CHECKED("A"), UNCHECKED("D")
}

enum class StateShoppingCart() {
    NOTHING, GOOD, LOADING, ERROR, ORDER_SUCCESS
}
