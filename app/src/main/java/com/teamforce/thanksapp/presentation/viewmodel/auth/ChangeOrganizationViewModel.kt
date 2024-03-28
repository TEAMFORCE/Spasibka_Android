package com.teamforce.thanksapp.presentation.viewmodel.auth

import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.*
import com.teamforce.thanksapp.NotificationsRepository
import com.teamforce.thanksapp.data.SharedPreferencesWrapperUserData
import com.teamforce.thanksapp.data.entities.notifications.PushTokenEntity
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.repositories.ProfileRepository
import com.teamforce.thanksapp.domain.usecases.LoadProfileUseCase
import com.teamforce.thanksapp.model.domain.UserData
import com.teamforce.thanksapp.utils.Result
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChangeOrganizationViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    val userDataRepository: UserDataRepository,
    private val loadProfileUseCase: LoadProfileUseCase,
    private val sharedPreferences: SharedPreferencesWrapperUserData,
    private val notificationsRepository: NotificationsRepository,
    private val app: Application
) : AndroidViewModel(app) {

    private var token: String? = null
    private var telegramOrEmail: String? = null

    var authorizationType: String? = null


    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _verifyResult = MutableLiveData<Result<UserData>>()
    val verifyResult: LiveData<Result<UserData>> = _verifyResult

    private val _verifyError = MutableLiveData<Result<String>>()
    val verifyError: LiveData<Result<String>> = _verifyError

    private val _profile = MutableLiveData<ProfileModel>()
    val profile: LiveData<ProfileModel> = _profile
    private val _profileError = MutableLiveData<String>()

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    fun changeOrgWithTelegram(
        telegramId: String,
        codeFromTg: String,
        xCode: String,
        orgCode: String
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = profileRepository.changeOrganizationVerifyWithTelegram(
                    code = codeFromTg,
                    xId = telegramId,
                    xCode = xCode,
                    orgCode = orgCode
                )) {
                    is ResultWrapper.Success -> {
                        if (result.value.token != null) {
                            _verifyResult.postValue(
                                Result.Success(
                                    UserData(
                                        result.value.token,
                                        telegramOrEmail
                                    )
                                )
                            )
                            updatePushToken()
                        }
                    }
                    is ResultWrapper.GenericError ->
                        _verifyError.postValue(Result.Error(result.error + " " + result.code))

                    is ResultWrapper.NetworkError ->
                        _internetError.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun changeOrgWithEmail(emailId: String, codeFromEmail: String, xCode: String, orgCode: String) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = profileRepository.changeOrganizationVerifyWithEmail(
                    code = codeFromEmail,
                    xEmail = emailId,
                    xCode = xCode,
                    orgCode = orgCode
                )) {
                    is ResultWrapper.Success -> {
                        if (result.value.token != null) {
                            _verifyResult.postValue(
                                Result.Success(
                                    UserData(
                                        result.value.token,
                                        telegramOrEmail
                                    )
                                )
                            )
                            updatePushToken()
                        }
                    }
                    is ResultWrapper.GenericError ->
                        _verifyError.postValue(Result.Error(result.error + " " + result.code))

                    is ResultWrapper.NetworkError ->
                        _internetError.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun saveDataForChangeOrg(
        authToken: String?,
        telegramOrEmail: String?
    ): Boolean {
        return userDataRepository.saveCredentials(
            authToken, telegramOrEmail, userDataRepository.getUserName())
    }


    fun loadUserProfile() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = loadProfileUseCase()) {
                    is ResultWrapper.Success -> {
                        _profile.postValue(result.value!!)
                        Log.d("Token", "Сохраняем id профиля ${result.value.profile.id}")
                        userDataRepository.saveProfileId(result.value.profile.id)
                        userDataRepository.saveUsername(result.value.profile.tgName)
                    }
                    is ResultWrapper.GenericError ->
                        _profileError.postValue(result.error + " " + result.code)

                    is ResultWrapper.NetworkError ->
                        _internetError.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }


    private fun updatePushToken() {
        val token = sharedPreferences.pushToken
        if (token != null) {
            val deviceId = Settings.Secure.getString(
                app.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            viewModelScope.launch {
                notificationsRepository.updatePushToken(
                    PushTokenEntity(
                        token = token,
                        device = deviceId
                    )
                )
            }
        }

    }

    fun getXcode() = userDataRepository.getXcode()
    fun getXId() = userDataRepository.getXid()
    fun getXEmail() = userDataRepository.getXEmail()
    fun getOrgId() = userDataRepository.getOrgCode()
    fun getAuthType() = userDataRepository.getAuthType()
    fun deleteCredentialsForChangeOrg() = userDataRepository.deleteCredentialsForChangeOrg()

}