package com.teamforce.thanksapp.presentation.activity

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.teamforce.thanksapp.NotificationsRepository
import com.teamforce.thanksapp.PushNotificationService
import com.teamforce.thanksapp.data.response.errors.AuthErrorResponse
import com.teamforce.thanksapp.data.response.onboarding.InvitationOrganizationResponse
import com.teamforce.thanksapp.domain.interactors.BrandingInteractors
import com.teamforce.thanksapp.domain.interactors.OnBoardingInteractor
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.usecases.LoadProfileUseCase
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.isNotNullOrEmptyMy
import com.teamforce.thanksapp.utils.isNullOrEmptyMy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val loadProfileUseCase: LoadProfileUseCase,
    private val notificationsRepository: NotificationsRepository,
    private val onBoardingInteractor: OnBoardingInteractor,
    private val brandingInteractors: BrandingInteractors
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading


    private val _isSuccessfulOperation = MutableLiveData<Boolean>()
    val isSuccessfulOperation: LiveData<Boolean> = _isSuccessfulOperation

    private val _orgByInvitation = MutableLiveData<InvitationOrganizationResponse?>()
    val orgByInvitation: LiveData<InvitationOrganizationResponse?> = _orgByInvitation.distinctUntilChanged()

    private val _orgByInvitationError = MutableLiveData<String>()
    val orgByInvitationError: LiveData<String> = _orgByInvitationError

    private val _profile = MutableLiveData<ProfileModel>()
    val profile: LiveData<ProfileModel> = _profile

    private val _profileError = MutableLiveData<String?>()
    val profileError: LiveData<String?> = _profileError

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError


    private var isHeadOfACurrentCommunity: Boolean = false

    fun loadLocalBrandTheme(){
        brandingInteractors.getLocalOrganizationBrand()
    }


    fun loadUserProfile() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = loadProfileUseCase()) {
                    is ResultWrapper.Success -> {
                        _profile.postValue(result.value!!)
                        isHeadOfACurrentCommunity = result.value.profile.isHeadOfACurrentCommunity
                    }
                    is ResultWrapper.GenericError ->{
                        _profileError.postValue("${result.code}")
                    }

                    is ResultWrapper.NetworkError ->
                        _internetError.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun logout(deviceId: String) {
        //при выходе из аккаунта удаляем пуш токен, чтобы не приходили уведомления
        viewModelScope.launch {
            try {
                notificationsRepository.deletePushToken(
                    deviceId
                )
            } catch (e: Exception) {
                Log.d(PushNotificationService.TAG, "logout: error $e")
            }
            userDataRepository.logout()
        }
    }


    fun isUserAuthorized(): Boolean {
        return userDataRepository.getAuthToken().isNotNullOrEmptyMy()
    }
    fun isCurrentOrgNotNull(): Boolean {
        return !userDataRepository.getCurrentOrg().isNullOrEmptyMy()
    }

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
                    is ResultWrapper.GenericError ->{
                        val statusError = Gson().fromJson(result.error, AuthErrorResponse::class.java).status
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