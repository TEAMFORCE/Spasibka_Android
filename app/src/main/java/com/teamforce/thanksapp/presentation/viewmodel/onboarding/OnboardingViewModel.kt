package com.teamforce.thanksapp.presentation.viewmodel.onboarding

import androidx.lifecycle.*
import com.teamforce.thanksapp.data.response.onboarding.InvitationOrganizationResponse
import com.teamforce.thanksapp.domain.interactors.OnBoardingInteractor
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onBoardingInteractor: OnBoardingInteractor,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _orgName = MutableLiveData<String?>()
    val orgName: LiveData<String?> = _orgName




    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    fun createCommunity(name: String) {
        _isLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = onBoardingInteractor.createCommunity(name)) {
                    is ResultWrapper.Success -> {
                        _internetError.postValue(false)
                        userDataRepository.saveUserCurrentOrgId(result.value.organization_id)
                        _orgName.postValue(name)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _internetError.postValue(false)
                            _orgName.postValue(null)
                            _error.postValue(result.error)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                            _orgName.postValue(null)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }


    private val _orgByInvitation = MutableLiveData<InvitationOrganizationResponse?>()
    val orgByInvitation: LiveData<InvitationOrganizationResponse?> =
        _orgByInvitation.distinctUntilChanged()

    fun getOrgByInvitation(
        invite: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = onBoardingInteractor.getOrgByInvitation(invite)) {
                    is ResultWrapper.Success -> {
                        _orgByInvitation.postValue(result.value)
                    }
                    is ResultWrapper.GenericError -> {}

                    is ResultWrapper.NetworkError ->
                        _internetError.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }
}