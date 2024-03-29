package com.teamforce.thanksapp.presentation.viewmodel.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.onboarding.InviteLinkResponse
import com.teamforce.thanksapp.domain.interactors.OnBoardingInteractor
import com.teamforce.thanksapp.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class InviteMembersViewModel @Inject constructor(
    private val onBoardingInteractor: OnBoardingInteractor
): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _invitationData = MutableLiveData<InviteLinkResponse?>()
    val invitationData: LiveData<InviteLinkResponse?> = _invitationData

    private val _invitationError = MutableLiveData<Boolean?>()
    val invitationError: LiveData<Boolean?> = _invitationError

    fun getInvitationData(organizationId: String?) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = onBoardingInteractor.getInviteLink(organizationId?.toIntOrNull())) {
                    is ResultWrapper.Success -> {
                        _invitationData.postValue(result.value)
                        _isLoading.postValue(false)
                        _invitationError.postValue(false)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            if(result.code == 403){
                                _isLoading.postValue(false)
                                _invitationError.postValue(true)
                            }

                        } else if (result is ResultWrapper.NetworkError) {

                        }
                    }
                }
            }
        }
    }
}