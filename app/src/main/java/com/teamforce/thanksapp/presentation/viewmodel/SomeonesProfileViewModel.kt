package com.teamforce.thanksapp.presentation.viewmodel

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.UserListItem
import com.teamforce.thanksapp.data.response.UserListItem.UserBean
import com.teamforce.thanksapp.domain.mappers.proflle.ProfileMapper
import com.teamforce.thanksapp.domain.models.profile.LocationModel
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.usecases.LoadProfileOfAnotherUserUseCase
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SomeonesProfileViewModel @Inject constructor(
    private val loadAnotherProfileUseCase: LoadProfileOfAnotherUserUseCase,
    private val profileMapper: ProfileMapper,
    val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _anotherProfile = MutableLiveData<ProfileModel>()
    val anotherProfile: LiveData<ProfileModel> = _anotherProfile

    private val _location = MutableLiveData<LocationModel?>()
    val location: LiveData<LocationModel?> = _location

    private val _anotherProfileError = MutableLiveData<String>()
    val profileError: LiveData<String> = _anotherProfileError

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    fun getUserBean(): UserBean? {
        return _anotherProfile.value?.let {
            profileMapper.mapUserProfileModelToProfileBean(it)
        }
    }


    fun getProfileId() = userDataRepository.getProfileId()

    fun loadAnotherUserProfile(userId: Int) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = loadAnotherProfileUseCase(userId = userId)) {
                    is ResultWrapper.Success -> {
                        _anotherProfile.postValue(result.value!!)
                        _location.postValue(result.value.profile.location)
                    }
                    is ResultWrapper.GenericError ->
                        _anotherProfileError.postValue(result.error + " " + result.code)

                    is ResultWrapper.NetworkError ->
                        _internetError.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }

}