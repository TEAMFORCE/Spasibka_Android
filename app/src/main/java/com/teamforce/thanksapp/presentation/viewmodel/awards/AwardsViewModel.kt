package com.teamforce.thanksapp.presentation.viewmodel.awards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.domain.interactors.AwardsInteractor
import com.teamforce.thanksapp.domain.models.awards.CategoryAwardsModel
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AwardsViewModel @Inject constructor(
    private val awardsInteractor: AwardsInteractor
): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _awards = MutableLiveData<List<CategoryAwardsModel>>()
    val awards: LiveData<List<CategoryAwardsModel>> = _awards


    fun loadAwards(showOnlyMyAwards: Boolean = false) {
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
}