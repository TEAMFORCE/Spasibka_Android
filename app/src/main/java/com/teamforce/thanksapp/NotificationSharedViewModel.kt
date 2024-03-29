package com.teamforce.thanksapp

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.SharedPreferencesWrapperUserData
import com.teamforce.thanksapp.data.entities.notifications.PushTokenEntity
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.isNullOrEmptyMy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("HardwareIds")
@HiltViewModel
class NotificationSharedViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val notificationsRepository: NotificationsRepository,
    sharedPreferences: SharedPreferencesWrapperUserData,
    app: Application
) : AndroidViewModel(app) {


    private val _userData = MutableLiveData<UserData?>(null)
    val userData: LiveData<UserData?> = _userData

    fun getUserData() {
        _userData.postValue(UserData(username = getUsername(), avatar = getAvatar()))
    }
    private fun getAvatar(): String? {
        return userDataRepository.getUserAvatar()
    }

   private fun getUsername(): String? {
        return if(userDataRepository.getName().isNullOrEmpty()){
            userDataRepository.getUserName()
        }else{
            userDataRepository.getName()
        }
    }

    init {
        if (sharedPreferences.pushToken != null) {
            val id = Settings.Secure.getString(
                app.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            updatePushToken(token = sharedPreferences.pushToken!!, deviceId = id)
        }
    }

    private var counter = 0
    val state: MediatorLiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(notificationsRepository.state) {
            if (it is NotificationStates.NotificationReceived) {
                counter++
                value = counter
            }
            if (it is NotificationStates.PushTokenUpdated) {
                updatePushToken(token = it.token, deviceId = it.deviceId)
            }
        }
    }

    fun checkNotifications() {
        viewModelScope.launch {
            when (val result = notificationsRepository.getUnreadNotificationsAmount()) {
                is ResultWrapper.Success -> state.value = result.value.unreadNotificationsAmount
                else -> state.value = 0
            }
        }
    }

    fun dropNotificationCounter() {
        counter = 0
        state.value = counter
    }

    private fun updatePushToken(token: String, deviceId: String) {
        viewModelScope.launch {
            notificationsRepository.updatePushToken(
                PushTokenEntity(
                    device = deviceId,
                    token = token
                )
            )
        }
    }

    companion object {
        data class UserData(
            val username: String?,
            val avatar: String?
        )
    }
}