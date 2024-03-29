package com.teamforce.thanksapp.presentation.viewmodel.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.NotificationsRepository
import com.teamforce.thanksapp.PushNotificationService
import com.teamforce.thanksapp.data.entities.auth.AuthVkResponse
import com.teamforce.thanksapp.data.entities.profile.OrganizationModel
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodRequest
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodResponse
import com.teamforce.thanksapp.data.response.ChangeOrgResponse
import com.teamforce.thanksapp.data.response.GetOrgSettings
import com.teamforce.thanksapp.data.response.onboarding.InviteLinkResponse
import com.teamforce.thanksapp.domain.interactors.OnBoardingInteractor
import com.teamforce.thanksapp.domain.interactors.ProfileSettingsInteractor
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.repositories.AuthRepository
import com.teamforce.thanksapp.domain.repositories.ProfileRepository
import com.teamforce.thanksapp.domain.usecases.LoadProfileUseCase
import com.teamforce.thanksapp.presentation.viewmodel.AuthorizationType
import com.teamforce.thanksapp.utils.Locals.Lang
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.SingleLiveEvent
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.toResultState
import com.vk.auth.main.VkClientAuthLib
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val userDataRepository: UserDataRepository,
    private val notificationsRepository: NotificationsRepository,
    private val onBoardingInteractor: OnBoardingInteractor,
    private val loadProfileUseCase: LoadProfileUseCase,
    private val profileSettingsInteractor: ProfileSettingsInteractor,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _organisationSettings = MutableLiveData<GetOrgSettings?>()
    val organisationSettings: LiveData<GetOrgSettings?> = _organisationSettings

    private val _organisations = MutableLiveData<List<OrganizationModel>?>()
    val organizations: LiveData<List<OrganizationModel>?> = _organisations
    private val _organisationsError = MutableLiveData<String>()
    val organizationsError: LiveData<String> = _organisationsError

    private val _changeOrgResponse = MutableLiveData<ChangeOrgResponse?>()
    val changeOrgResponse: LiveData<ChangeOrgResponse?> = _changeOrgResponse

    private val _changeOrgError = MutableLiveData<String?>()
    val changeOrgError: LiveData<String?> = _changeOrgError

    var authorizationType: AuthorizationType? = null

    private val _profile = MutableLiveData<ProfileModel>()
    val profile: LiveData<ProfileModel> = _profile

    private val _error = SingleLiveEvent<String>()
    val error: SingleLiveEvent<String> = _error


    fun saveDataForChangeOrg(
        authToken: String?
    ): Boolean {
        return userDataRepository.saveCredentials(
            authToken, "VK", userDataRepository.getUserName())
    }

    fun getVkAccessToken(): String? = userDataRepository.getVkAccessToken()

    fun loadUserProfile() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = loadProfileUseCase()) {
                    is ResultWrapper.Success -> {
                        _profile.postValue(result.value!!)
                        userDataRepository.saveProfileId(result.value.profile.id)
                        userDataRepository.saveUsername(result.value.profile.tgName)
                    }
                    is ResultWrapper.GenericError ->
                        _error.postValue("${result.code} ${result.error}")

                    is ResultWrapper.NetworkError -> {}
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun clearChangeOrgResponse() = _changeOrgResponse.postValue(null)

    fun changeOrg(
        orgId: Int,
        accessToken: String? = null,
    ) {
        viewModelScope.launch {
            profileRepository.changeOrganization(orgId, accessToken).toResultState(
                onSuccess = {
                    Log.d("Token", "Status запроса: ${it}")
                    _changeOrgResponse.postValue(it)
                    if (it.target == "telegram") {
                        authorizationType = AuthorizationType.TELEGRAM
                    } else if (it.target == "email") {
                        authorizationType = AuthorizationType.EMAIL
                    }
                },
                onLoading = {
                    _isLoading.postValue(it)
                },
                onError = { error, code ->
                    _changeOrgError.postValue("$code $error")
                }
            )
        }
    }

    fun deletePushToken(deviceId: String) {
        //при выходе из аккаунта удаляем пуш токен, чтобы не приходили уведомления
        viewModelScope.launch {
            try {
                notificationsRepository.deletePushToken(
                    deviceId
                )
            } catch (e: Exception) {
                Log.d(PushNotificationService.TAG, "logout: error $e")
            }
        }
    }


    fun loadUserOrganizations() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = profileRepository.getOrganizations()) {
                    is ResultWrapper.Success -> {
                        _organisations.postValue(result.value)
                    }
                    is ResultWrapper.GenericError ->
                        _organisationsError.postValue(result.error + " " + result.code)

                    is ResultWrapper.NetworkError ->
                        _organisationsError.postValue("Ошибка сети")
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun loadOrganizationSettings() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = profileRepository.getOrgSettings()) {
                    is ResultWrapper.Success -> {
                        _organisationSettings.postValue(result.value)
                    }
                    is ResultWrapper.GenericError ->
                        _organisationsError.postValue(result.error + " " + result.code)

                    is ResultWrapper.NetworkError ->
                        _organisationsError.postValue("Ошибка сети")
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun saveCredentialsForChangeOrg() {
        userDataRepository.saveCredentialsForChangeOrg(
            xCode = _changeOrgResponse.value?.xCode,
            xId = _changeOrgResponse.value?.tgCode,
            orgCode = _changeOrgResponse.value?.orgId,
            xEmail = _changeOrgResponse.value?.email,
            authType = authorizationType ?: AuthorizationType.TELEGRAM
        )
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
            VkClientAuthLib.logout()
        }
    }

    private val _createCommunity = MutableLiveData<CreateCommunityWithPeriodResponse?>()
    val createCommunity: LiveData<CreateCommunityWithPeriodResponse?> = _createCommunity

    fun createCommunity(request: CreateCommunityWithPeriodRequest) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = profileSettingsInteractor.createCommunity(request)) {
                    is ResultWrapper.Success -> {
                        _createCommunity.postValue(result.value)
                    }
                    is ResultWrapper.GenericError ->
                        _error.postValue(result.error + " " + result.code)

                    is ResultWrapper.NetworkError -> {}
                }
                _isLoading.postValue(false)
            }
        }
    }

    private val _authVk = SingleLiveEvent<AuthVkResponse?>()
    val authVk: SingleLiveEvent<AuthVkResponse?> = _authVk

    fun authThroughVk(
        accessToken: String
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = authRepository.authThroughVk(accessToken, null)) {
                    is ResultWrapper.Success -> {
                        _authVk.postValue(result.value)
                    }

                    is ResultWrapper.GenericError -> {

                    }

                    is ResultWrapper.NetworkError -> {}
                }
                _isLoading.postValue(false)
            }
        }
    }

    private val _changeLang = MutableLiveData<Boolean?>()
    val changeLang: LiveData<Boolean?> = _changeLang

    private val _changeLangError = MutableLiveData<String?>()
    val changeLangError: LiveData<String?> = _changeLangError

    fun updateLangInProfileSettings(
        lang: Lang
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = profileRepository.updateProfileLang(lang)) {
                    is ResultWrapper.Success -> {
                        _changeLang.postValue(true)
                    }

                    is ResultWrapper.GenericError -> {
                        _changeLang.postValue(false)
                    }

                    is ResultWrapper.NetworkError -> {
                        _changeLang.postValue(false)
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

}