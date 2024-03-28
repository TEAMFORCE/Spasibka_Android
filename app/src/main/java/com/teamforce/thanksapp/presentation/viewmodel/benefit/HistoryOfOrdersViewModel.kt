package com.teamforce.thanksapp.presentation.viewmodel.benefit

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.teamforce.thanksapp.data.entities.benefit.Category
import com.teamforce.thanksapp.data.entities.benefit.Order
import com.teamforce.thanksapp.domain.models.benefit.BenefitModel
import com.teamforce.thanksapp.domain.repositories.BenefitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryOfOrdersViewModel @Inject constructor(
    private val benefitRepository: BenefitRepository
): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _categories = MutableLiveData<List<Category>?>()
    val categories: MutableLiveData<List<Category>?> = _categories

    private val _statusList = MutableLiveData<List<String>?>(null)
    val statusList: MutableLiveData<List<String>?> = _statusList

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: MutableLiveData<Boolean> = _internetError

    fun setStatus(listOfStatus: List<String>?){
        _statusList.postValue(listOfStatus)
    }

    fun resetStatus(){
        _statusList.postValue(null)
    }

    fun getOrders(marketId: Int): Flow<PagingData<Order>> {
        return benefitRepository.getOrdersInHistory(listOfStatus = statusList.value, marketId = marketId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        ).cachedIn(viewModelScope)
    }



}