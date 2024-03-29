package com.teamforce.thanksapp.presentation.viewmodel.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.teamforce.thanksapp.data.response.errors.AuthErrorResponse
import com.teamforce.thanksapp.data.response.onboarding.InvitationOrganizationResponse
import com.teamforce.thanksapp.domain.interactors.OnBoardingInteractor
import com.teamforce.thanksapp.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class JoinToOrganizationViewModel @Inject constructor(
    private val onBoardingInteractor: OnBoardingInteractor,
) : ViewModel() {

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _orgByInvitation = MutableLiveData<InvitationOrganizationResponse?>()
    val orgByInvitation: LiveData<InvitationOrganizationResponse?> =
        _orgByInvitation.distinctUntilChanged()

    private val _orgByInvitationError = MutableLiveData<String>()
    val orgByInvitationError: LiveData<String> = _orgByInvitationError

    fun getOrgByInvitation(
        invite: String
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = onBoardingInteractor.getOrgByInvitation(invite)) {
                    is ResultWrapper.Success -> {
                        _orgByInvitation.postValue(result.value)
                    }
                    is ResultWrapper.GenericError -> {
                        val statusError =
                            Gson().fromJson(result.error, AuthErrorResponse::class.java).status
                        _orgByInvitationError.postValue(statusError)
                    }

                    is ResultWrapper.NetworkError ->
                        _internetError.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }
}