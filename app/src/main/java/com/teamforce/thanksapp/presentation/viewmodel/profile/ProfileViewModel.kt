package com.teamforce.thanksapp.presentation.viewmodel.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.NotificationsRepository
import com.teamforce.thanksapp.PushNotificationService
import com.teamforce.thanksapp.data.entities.location.toLocationModel
import com.teamforce.thanksapp.data.entities.profile.ProfileStatsEntity
import com.teamforce.thanksapp.domain.models.profile.LocationModel
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.repositories.LocationRepository
import com.teamforce.thanksapp.domain.repositories.ProfileRepository
import com.teamforce.thanksapp.domain.usecases.LoadProfileUseCase
import com.teamforce.thanksapp.presentation.fragment.profileScreen.ProfileFragment
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val loadProfileUseCase: LoadProfileUseCase,
    private val profileRepository: ProfileRepository,
    private val notificationsRepository: NotificationsRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading


    private val _isSuccessfulOperation = MutableLiveData<Boolean>()
    val isSuccessfulOperation: LiveData<Boolean> = _isSuccessfulOperation

    private val _profile = MutableLiveData<ProfileModel>()
    val profile: LiveData<ProfileModel> = _profile

    private val _profileError = MutableLiveData<String?>()
    val profileError: LiveData<String?> = _profileError

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    private val _location = MutableLiveData<LocationModel?>()
    val location: LiveData<LocationModel?> = _location

    private val _tags =
        MutableLiveData<com.teamforce.thanksapp.utils.Result<List<ProfileStatsEntity>>>()
    val tags: LiveData<com.teamforce.thanksapp.utils.Result<List<ProfileStatsEntity>>> =
        _tags

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
                        _location.postValue(result.value.profile.location)
                    }

                    is ResultWrapper.GenericError ->
                        _profileError.postValue("${result.code}")

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

    fun getStats() {
        viewModelScope.launch {
            val res = profileRepository.getProfileStats(null, 1)
            when (res) {
                is ResultWrapper.Success -> {
                    res.value.forEach {
                        Log.d(ProfileFragment.TAG, "getStats: ${it.name} ${it.amount} ${it.sum}")
                    }
                    _tags.postValue(com.teamforce.thanksapp.utils.Result.Success(res.value))
                }

                else -> {
                    _tags.postValue(com.teamforce.thanksapp.utils.Result.Error(""))
                }
            }
        }
    }

    fun isUserAuthorized() = userDataRepository.getAuthToken() != null

}
