package com.teamforce.thanksapp.presentation.viewmodel

import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.teamforce.thanksapp.NotificationsRepository
import com.teamforce.thanksapp.PushNotificationService
import com.teamforce.thanksapp.data.SharedPreferencesWrapperUserData
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.auth.AuthVkResponse
import com.teamforce.thanksapp.data.entities.notifications.PushTokenEntity
import com.teamforce.thanksapp.data.request.AuthorizationRequest
import com.teamforce.thanksapp.data.request.ChooseOrgRequest
import com.teamforce.thanksapp.data.request.VerificationRequest
import com.teamforce.thanksapp.data.response.AuthResponse
import com.teamforce.thanksapp.data.response.VerificationResponse
import com.teamforce.thanksapp.data.response.errors.AuthErrorResponse
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.repositories.AuthRepository
import com.teamforce.thanksapp.domain.usecases.LoadProfileUseCase
import com.teamforce.thanksapp.model.domain.UserData
import com.teamforce.thanksapp.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val thanksApi: ThanksApi,
    val userDataRepository: UserDataRepository,
    private val loadProfileUseCase: LoadProfileUseCase,
    private val sharedPreferences: SharedPreferencesWrapperUserData,
    private val notificationsRepository: NotificationsRepository,
    private val app: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(app)
{
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var xId: String? = null
    private var xEmail: String? = null
    private var xCode: String? = null
    private var token: String? = null
    private var telegramOrEmail: String? = null

    var authorizationType: AuthorizationType? = null

    private val _authResult = MutableLiveData<Result<Boolean>>()
    val authResult: LiveData<Result<Boolean>> = _authResult

    private val _authError = MutableLiveData<Result<AuthErrorResponse>>()
    val authError: LiveData<Result<AuthErrorResponse>> = _authError


    private val _organizations = MutableLiveData<List<AuthResponse.Organization>?>()
    val organizations: LiveData<List<AuthResponse.Organization>?> = _organizations

    private val _verifyResult = MutableLiveData<Result<UserData>>()
    val verifyResult: LiveData<Result<UserData>> = _verifyResult

    private val _needChooseOrg = MutableLiveData<Boolean>()
    val needChooseOrg: LiveData<Boolean> = _needChooseOrg

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    fun isUserAuthorized(): Boolean {
        Log.e("LoginFragment", " TOKEN - ${userDataRepository.getAuthToken()}")
        return userDataRepository.getAuthToken().isNotNullOrEmptyMy()
    }
    fun isCurrentOrgNull(): Boolean {
        Log.e("LoginFragment", "ORG ID -  ${userDataRepository.getCurrentOrg()}")
        return userDataRepository.getCurrentOrg().isNullOrEmptyMy()
    }

    fun saveCurrentLogin(tgOrEmail: String) = userDataRepository.saveCurrentLogin(tgOrEmail)

    fun saveVkAccessToken(accessToken: String) = userDataRepository.saveVkAccessToken(accessToken)

    fun getCurrentLogin() = userDataRepository.getCurrentLogin()

    fun clearAuthToken() = userDataRepository.clearAuthToken()


    fun logout(deviceId: String) {
        userDataRepository.logout()
        xId = null
        xEmail = null
        xCode = null
        token = null
        telegramOrEmail = null

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

    private val _profile = MutableLiveData<ProfileModel>()
    val profile: LiveData<ProfileModel> = _profile
    private val _profileError = MutableLiveData<String>()

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
                        userDataRepository.saveUserAvatar(result.value.profile.photo)
                        // Сохранение tgName или username, если первое null
                        if (result.value.profile.tgName == null) {
                            userDataRepository.saveUsername(result.value.username)
                        } else {
                            userDataRepository.saveUsername(result.value.profile.tgName)
                        }
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

    fun authorizeUser(telegramIdOrEmail: String, sharedKey: String?) {
        telegramOrEmail = telegramIdOrEmail
        _isLoading.postValue(true)
        viewModelScope.launch { callAuthorizationEndpoint(telegramIdOrEmail, sharedKey, Dispatchers.Default) }
    }

    private suspend fun callAuthorizationEndpoint(
        telegramIdOrEmail: String,
        sharedKey: String?,
        coroutineDispatcher: CoroutineDispatcher,
    ) {
        withContext(coroutineDispatcher) {
            thanksApi.authorization(AuthorizationRequest(login = telegramIdOrEmail, shared_key = sharedKey))
                .enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(
                        call: Call<AuthResponse>,
                        response: Response<AuthResponse>,
                    ) {
                        _isLoading.postValue(false)
                        if (response.code() == 200) {
                            _organizations.postValue(response.body()?.organizations)
                            Log.d("Token", "Status запроса: ${response.body().toString()}")
                            if (response.body()?.xTelegram != null || response.body()?.isTest == true) {
                                xId = response.body()?.xTelegram
                                xCode = response.body()?.xCode
                                authorizationType = AuthorizationType.TELEGRAM
                                _needChooseOrg.postValue(false)
                            } else if (response.body()?.xEmail != null) {
                                xEmail = response.body()?.xEmail
                                xCode = response.body()?.xCode
                                authorizationType = AuthorizationType.EMAIL
                                _needChooseOrg.postValue(false)
                            } else if (response.body()?.status
                                    .toString() == "Необходимо выбрать организацию"
                            ) {
                                _needChooseOrg.postValue(true)
                            } else {
                                // Когда не один из вариантов не подошел выводим ошибку
                                _authResult.postValue(Result.Error("${response.message()} ${response.code()}"))
                            }
                            _authResult.postValue(Result.Success(true))
                        } else if (response.code() == 404){
                            _authResult.postValue(Result.Error("Пользователь не найден"))
                        }else {
                            try {
                                val error = Gson().fromJson(response.errorBody()!!.string(), AuthErrorResponse::class.java)
                                _authResult.postValue(Result.Error("${error.status}"))
                            }catch (_: Exception){
                                _authResult.postValue(Result.Error("Something went wrong"))
                            }
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        _isLoading.postValue(false)
                        _authResult.postValue(Result.Error(t.message ?: "Something went wrong"))
                        _internetError.postValue(true)
                    }
                })
        }
    }

    fun chooseOrg(userId: Int, orgId: Int?, login: String) {
        _isLoading.postValue(true)
        viewModelScope.launch { callChooseOrgEndpoint(userId, orgId, login, Dispatchers.Default) }
    }

    private suspend fun callChooseOrgEndpoint(
        userId: Int,
        orgId: Int?,
        login: String,
        coroutineDispatcher: CoroutineDispatcher,
    ) {
        withContext(coroutineDispatcher) {
            thanksApi.chooseOrganization(login, ChooseOrgRequest(userId, orgId))
                .enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(
                        call: Call<AuthResponse>,
                        response: Response<AuthResponse>,
                    ) {
                        _isLoading.postValue(false)
                        if (response.code() == 200) {
                            if (response.body()?.status.toString() == "Код отправлен в телеграм") {
                                xId = response.headers().get("X-Telegram")
                                authorizationType = AuthorizationType.TELEGRAM
                                _needChooseOrg.postValue(false)
                            }
                            if (response.body()?.status
                                    .toString() == "Код отправлен на указанную электронную почту"
                            ) {
                                xEmail = response.headers().get("X-Email")
                                authorizationType = AuthorizationType.EMAIL
                                _needChooseOrg.postValue(false)
                            }
                            xCode = response.headers().get("X-Code")
                            Log.d("Token", "Status запроса: ${response.body().toString()}")
                            _authResult.postValue(Result.Success(true))
                        } else {
                            _authResult.postValue(Result.Error("${response.message()} ${response.code()}"))
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        _isLoading.postValue(false)
                        _authResult.postValue(Result.Error(t.message ?: "Something went wrong"))
                        _internetError.postValue(true)
                    }
                })
        }
    }

    fun chooseOrgThroughVk(
        userId: Int,
        orgId: Int?,
        accessToken: String
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = authRepository.chooseOrgThroughVk(userId, orgId, accessToken)) {
                    is ResultWrapper.Success -> {
                        _authVk.postValue(result.value)
                    }

                    is ResultWrapper.GenericError ->{

                    }

                    is ResultWrapper.NetworkError ->
                        _internetError.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun verifyCodeTelegram(codeFromTg: String, orgId: Int?, sharedKey: String?) {
        _isLoading.postValue(true)
        viewModelScope.launch { callVerificationEndpointTelegram(codeFromTg, orgId, sharedKey, Dispatchers.Default) }
    }

    private suspend fun callVerificationEndpointTelegram(
        code: String,
        orgId: Int?,
        sharedKey: String?,
        coroutineDispatcher: CoroutineDispatcher,
    ) {
        withContext(coroutineDispatcher) {
            thanksApi.verificationWithTelegram(xId, xCode, VerificationRequest(code = code, organization_id = orgId, shared_key = sharedKey))
                .enqueue(object : Callback<VerificationResponse> {
                    override fun onResponse(
                        call: Call<VerificationResponse>,
                        response: Response<VerificationResponse>,
                    ) {
                        _isLoading.postValue(false)
                        if (response.code() == 200) {
                            token = response.body()?.token
                            if (token == null) {
                                _verifyResult.postValue(Result.Error("token == null!!!!!"))
                            } else {
                                _verifyResult.postValue(
                                    Result.Success(
                                        UserData(
                                            token,
                                            telegramOrEmail
                                        )
                                    )
                                )
                                updatePushToken()
                            }
                        } else {
                            _verifyResult.postValue(Result.Error(response.body()?.reason ?: ""))
                        }
                    }

                    override fun onFailure(call: Call<VerificationResponse>, t: Throwable) {
                        _isLoading.postValue(false)
                        _verifyResult.postValue(Result.Error(t.message ?: "Something went wrong"))
                        _internetError.postValue(true)
                    }
                })
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

    fun verifyCodeEmail(codeFromTg: String, orgId: Int?, sharedKey: String?) {
        _isLoading.postValue(true)
        viewModelScope.launch { callVerificationEndpointEmail(codeFromTg, orgId, sharedKey, Dispatchers.Default) }
    }

    private suspend fun callVerificationEndpointEmail(
        code: String,
        orgId: Int?,
        sharedKey: String?,
        coroutineDispatcher: CoroutineDispatcher,
    ) {
        withContext(coroutineDispatcher) {
            Log.d("Token", "xEmail:${xEmail} --- xCode:${xCode}---- verifyCode:${code} ")
            thanksApi.verificationWithEmail(xEmail, xCode, VerificationRequest(code = code, organization_id = orgId, shared_key = sharedKey))
                .enqueue(object : Callback<VerificationResponse> {
                    override fun onResponse(
                        call: Call<VerificationResponse>,
                        response: Response<VerificationResponse>,
                    ) {
                        _isLoading.postValue(false)
                        if (response.code() == 200) {
                            token = response.body()?.token
                            if (token == null) {
                                _verifyResult.postValue(Result.Error("token == null!!!!!"))
                            } else {
                                _verifyResult.postValue(
                                    Result.Success(
                                        UserData(
                                            token,
                                            telegramOrEmail
                                        )
                                    )
                                )
                            }
                        } else {
                            _verifyResult.postValue(Result.Error(response.message() + " " + response.code()))
                        }
                    }

                    override fun onFailure(call: Call<VerificationResponse>, t: Throwable) {
                        _isLoading.postValue(false)
                        _verifyResult.postValue(Result.Error(t.message ?: "Something went wrong"))
                        _internetError.postValue(true)
                    }
                })
        }
    }

    private val _authVk = MutableLiveData<AuthVkResponse?>()
    val authVk: LiveData<AuthVkResponse?> = _authVk

    fun authThroughVk(
        accessToken: String,
        sharedKey: String?
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = authRepository.authThroughVk(accessToken, sharedKey)) {
                    is ResultWrapper.Success -> {
                        _authVk.postValue(result.value)
                        _organizations.postValue(result.value.organizations)

                    }

                    is ResultWrapper.GenericError ->{

                    }

                    is ResultWrapper.NetworkError ->
                        _internetError.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }
}
