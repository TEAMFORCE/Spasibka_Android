package com.teamforce.thanksapp.presentation.viewmodel.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.domain.interactors.OnBoardingInteractor
import com.teamforce.thanksapp.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsPeriodViewModel @Inject constructor(
    private val onBoardingInteractor: OnBoardingInteractor
): ViewModel() {

    private val _settingsScreenState = MutableLiveData(SettingScreenState.AUTO_SETTINGS)
    val settingsScreenState: MutableLiveData<SettingScreenState> = _settingsScreenState

    fun updateScreenState(state: SettingScreenState) = _settingsScreenState.postValue(state)

    enum class SettingScreenState(val state: String){
        AUTO_SETTINGS("auto_settings"), MANUAL_SETTINGS("manual_settings")
    }

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading


    private val _launchPeriod = MutableLiveData<Boolean>()
    val launchPeriod: LiveData<Boolean> = _launchPeriod

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    fun launchCommunityPeriod(
        startDate: String,
        endDate: String,
        startBalance: Int,
        startAdminBalance: Int
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result =
                    onBoardingInteractor.launchCommunityPeriod(
                        startDate,
                        endDate,
                        startBalance,
                        startAdminBalance
                    )) {
                    is ResultWrapper.Success -> {
                        _internetError.postValue(false)
                        _launchPeriod.postValue(true)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _internetError.postValue(false)
                            _launchPeriod.postValue(false)
                            _error.postValue(result.error)
                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                            _launchPeriod.postValue(false)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }
}