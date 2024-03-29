package com.teamforce.thanksapp.presentation.viewmodel.benefit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.teamforce.thanksapp.data.entities.benefit.AvailableMarketEntity
import com.teamforce.thanksapp.data.entities.benefit.Category
import com.teamforce.thanksapp.domain.models.benefit.BenefitModel
import com.teamforce.thanksapp.domain.repositories.BenefitRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BenefitListViewModel @Inject constructor(
    private val benefitRepository: BenefitRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _categories = MutableLiveData<List<Category>?>()
    val categories: MutableLiveData<List<Category>?> = _categories

    private val _categoriesError = MutableLiveData<String>()
    val categoriesError: MutableLiveData<String> = _categoriesError

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: MutableLiveData<Boolean> = _internetError

    private var _checkedIdCategory = MutableLiveData<Int>()
    val checkedIdCategory: MutableLiveData<Int> = _checkedIdCategory

    private var _searchString = MutableLiveData<String>()
    val searchString: MutableLiveData<String> = _searchString

    private var _price = MutableLiveData<Pair<String?, String?>>()
    val price: MutableLiveData<Pair<String?, String?>> = _price


    private var _filterMediatorLiveData = MediatorLiveData<String>()
    val filterMediatorLiveData: MediatorLiveData<String> = _filterMediatorLiveData

    private var _currentMarketId = MediatorLiveData<Int>()
    val currentMarketId = _currentMarketId


    private var _listOfAvailableMarkets = MutableLiveData<List<AvailableMarketEntity>?>()
    val listOfAvailableMarkets: MutableLiveData<List<AvailableMarketEntity>?> =
        _listOfAvailableMarkets


    init {
        _filterMediatorLiveData.addSource(
            currentMarketId
        ) { value ->
            _filterMediatorLiveData.value = value.toString()
        }
        _filterMediatorLiveData.addSource(
            _checkedIdCategory
        ) { value ->
            _filterMediatorLiveData.value = value.toString()
        }
        _filterMediatorLiveData.addSource(
            searchString
        ) { value ->
            _filterMediatorLiveData.value = value.toString()
        }
        _filterMediatorLiveData.addSource(
            price
        ) { value ->
            _filterMediatorLiveData.value = value.toString()
        }
    }

    fun setCurrentMarketId(marketId: Int){
        _currentMarketId.value = (marketId)
    }

    fun resetPrice() {
        setFilterPrice("", "")
    }

    fun setFilterPrice(fromTo: String?, upTo: String?) {
        _price.postValue(Pair(fromTo, upTo))
    }

    fun setCheckedIdCategory(value: Int?) {
        _checkedIdCategory.value = (value ?: 0)
    }

    fun setSearchString(value: String?) {
        _searchString.postValue(value ?: "")
    }


    fun getOffers(): Flow<PagingData<BenefitModel>>? {
        return currentMarketId.value?.let {
            benefitRepository.getOffers(
                checkedIdCategory.value ?: 0, marketId = it,
                searchString.value, minPrice = price.value?.first, maxPrice = price.value?.second
            ).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PagingData.empty()
            ).cachedIn(viewModelScope)
        }
    }

    fun loadCategories(
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = currentMarketId.value?.let {
                        benefitRepository.loadBenefitCategories(marketId = it)
                    }
                ) {
                    is ResultWrapper.Success -> {
                        _categories.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _categoriesError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun loadAvailableMarkets(
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = benefitRepository.getAvailableMarkets()) {
                    is ResultWrapper.Success -> {
                        _listOfAvailableMarkets.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }
}
