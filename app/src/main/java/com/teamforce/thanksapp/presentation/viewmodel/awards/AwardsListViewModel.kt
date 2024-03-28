package com.teamforce.thanksapp.presentation.viewmodel.awards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.domain.interactors.AwardsInteractor
import com.teamforce.thanksapp.domain.models.awards.AwardState
import com.teamforce.thanksapp.domain.models.awards.AwardsModel
import com.teamforce.thanksapp.domain.models.awards.CategoryAwardsModel
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AwardsListViewModel @Inject constructor(
    private val awardsInteractor: AwardsInteractor
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _awards = MutableLiveData<List<CategoryAwardsModel>>()
    val awards: LiveData<List<CategoryAwardsModel>> = _awards

    private val _awardsById = MutableLiveData<Map<Long, List<CategoryAwardsModel>>>()
    val awardsById: LiveData<Map<Long, List<CategoryAwardsModel>>> = _awardsById


    fun loadAwards(showOnlyMyAwards: Boolean) {
        viewModelScope.launch {
            awardsInteractor.loadAwards(showOnlyMyAwards).toResultState(
                onSuccess = {
                    _awards.postValue(it)
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

    fun loadAwardsById(categoryId: Long, showOnlyMyAwards: Boolean) {
        viewModelScope.launch {
//            val result =
//                if (showOnlyMyAwards) _myAwards.value?.filter { it.id == categoryId }
//                else _awards.value?.filter {
//                    it.id == categoryId
//                }
//            _awardsById.postValue(result)
            awardsInteractor.loadAwardsById(categoryId, showOnlyMyAwards).toResultState(
                onSuccess = {
                    addNewCategoryAwards(it)
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

    private val _myAwards = MutableLiveData<List<CategoryAwardsModel>>()
    val myAwards: LiveData<List<CategoryAwardsModel>> = _myAwards


    fun loadMyAwards() {
        viewModelScope.launch {
            awardsInteractor.loadAwards(true).toResultState(
                onSuccess = {
                    _myAwards.postValue(it)
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

    fun loadMyAwardsById(categoryId: Long) {
        viewModelScope.launch {
            awardsInteractor.loadAwardsById(categoryId, true).toResultState(
                onSuccess = {
                    addNewCategoryAwards(it)
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

    private fun addNewCategoryAwards(newCategoryAwards: List<CategoryAwardsModel>) {
        val currentAwardsMap = _awardsById.value ?: emptyMap()
        val updatedAwardsMap = updateAwardsMap(currentAwardsMap, newCategoryAwards)
        _awardsById.postValue(updatedAwardsMap)
    }

    private fun updateAwardsMap(
        currentAwardsMap: Map<Long, List<CategoryAwardsModel>>,
        newCategoryAwards: List<CategoryAwardsModel>
    ): Map<Long, List<CategoryAwardsModel>> {
        val updatedAwardsMap = currentAwardsMap.toMutableMap()
        for (categoryAwards in newCategoryAwards) {
            categoryAwards.id?.let { categoryId ->
                updatedAwardsMap[categoryId] = listOf(categoryAwards)
            }
        }
        return updatedAwardsMap
    }

    private val _awardState = MutableLiveData<AwardState>()
    val awardState: LiveData<AwardState> = _awardState

    fun setAwardState(state: AwardState) {
        _awardState.value = state
    }

//    fun setAwardWithDelay(bool: Boolean, delayMillis: Long) {
//        _isLoading.value = true
//        viewModelScope.launch {
//            delay(delayMillis)
//            _awardReceived.value = bool
//            _isLoading.value = false
//        }
//    }
}