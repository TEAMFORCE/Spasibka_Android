package com.teamforce.thanksapp.presentation.viewmodel.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.auth.AuthVkResponse
import com.teamforce.thanksapp.data.entities.profile.ContactEntity
import com.teamforce.thanksapp.data.request.UpdateProfileRequest
import com.teamforce.thanksapp.data.response.MessageAboutOperation
import com.teamforce.thanksapp.data.response.UpdateFewContactsResponse
import com.teamforce.thanksapp.data.response.UpdateProfileResponse
import com.teamforce.thanksapp.domain.models.profile.ContactModel
import com.teamforce.thanksapp.domain.models.profile.FormatOfWork
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.repositories.AuthRepository
import com.teamforce.thanksapp.domain.repositories.ProfileRepository
import com.teamforce.thanksapp.domain.usecases.LoadProfileUseCase
import com.teamforce.thanksapp.utils.*
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
class EditProfileViewModel @Inject constructor(
    private val thanksApi: ThanksApi,
    val userDataRepository: UserDataRepository,
    private val loadProfileUseCase: LoadProfileUseCase,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingUpdateAvatar = MutableLiveData<Boolean>()
    val isLoadingUpdateAvatar: LiveData<Boolean> = _isLoadingUpdateAvatar


    private val _updateProfile = SingleLiveEvent<UpdateProfileResponse?>()
    val updateProfile: SingleLiveEvent<UpdateProfileResponse?> = _updateProfile
    private val _updateProfileError = MutableLiveData<String?>()
    val updateProfileError: LiveData<String?> = _updateProfileError


    private val _isSuccessfulOperation = MutableLiveData<MessageAboutOperation>()
    val isSuccessfulOperation: LiveData<MessageAboutOperation> = _isSuccessfulOperation


    private val _profile = MutableLiveData<ProfileModel>()
    val profile: LiveData<ProfileModel> = _profile
    private val _profileError = MutableLiveData<String>()
    val profileError: LiveData<String> = _profileError


    private val _updateFewContact = MutableLiveData<UpdateFewContactsResponse>()
    val updateFewContact: LiveData<UpdateFewContactsResponse> = _updateFewContact
    private val _updateFewContactError = MutableLiveData<String>()
    val updateFewContactError: LiveData<String> = _updateFewContactError

    private val _gender = MutableLiveData<String?>()
    val gender: LiveData<String?> = _gender

    private val _birthday = MutableLiveData<String?>()
    val birthday: LiveData<String?> = _birthday

    fun resetState(){
        _updateProfile.postValue(null)
        _isLoading.postValue(false)
        _isLoadingUpdateAvatar.postValue(false)
        _gender.postValue(null)
        _birthday.postValue(null)
    }

    /**
     * @param date Строка, представляющая дату в формате ISO '2011-12-03' or '2011-12-03+01:00'.
     */
    fun setBirthday(date: String){
        _birthday.postValue(date)
    }

    fun setGender(gender: String?) {
        _gender.value = gender
    }

    fun getUserId() = userDataRepository.getProfileId()

    fun loadUserProfile() {
        _isLoading.postValue(true)
        viewModelScope.launch { callProfileEndpoint(Dispatchers.Default) }
    }

    private suspend fun callProfileEndpoint(
        coroutineDispatcher: CoroutineDispatcher
    ) {
        withContext(coroutineDispatcher) {

            when (val result = loadProfileUseCase()) {
                is ResultWrapper.Success -> {
                    _profile.postValue(result.value!!)
                    _birthday.postValue(result.value.profile.birthday)
                }
                else -> {
                    if (result is ResultWrapper.GenericError) {
                        _profileError.postValue(result.error + " " + result.code)

                    } else if (result is ResultWrapper.NetworkError) {
                        _profileError.postValue("Ошибка сети")
                    }
                }
            }
        }
    }


    fun loadUpdateProfile(
        userId: String,
        tgName: String?, surname: String?,
        firstName: String?, middleName: String?, nickname: String?, status: String?,  formatOfWork: String?,
        birthDay: String?, showYearOfBirth: Boolean, gender: String?
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = profileRepository.loadUpdateProfile(
                    userId = userId, tgName, surname,
                    firstName, middleName, nickname, status, formatOfWork,  birthDay, showYearOfBirth, gender
                )) {
                    is ResultWrapper.Success -> {
                        _updateProfile.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _updateProfileError.postValue("${result.code} ${result.error}")

                        } else if (result is ResultWrapper.NetworkError) {

                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun updateStatus(
        userId: String,
        status: String?,
        formatOfWork: String?

    ) {
        _isLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = profileRepository.updateStatus(
                    userId = userId, status, formatOfWork
                )) {
                    is ResultWrapper.Success -> {
                        _updateProfile.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _updateProfileError.postValue("${result.code} ${result.error}")

                        } else if (result is ResultWrapper.NetworkError) {

                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }


    fun loadUpdateFewContact(listContacts: List<ContactModel>) {
        val contacts = listContacts.map {
            ContactEntity(
                id = it.id,
                contact_id = it.contactId,
                contact_type = it.contactType
            )
        }
        _isLoading.postValue(true)
        viewModelScope.launch {
            callUpdateFewContactEndpoint(
                contacts,
                Dispatchers.Default
            )
        }
    }

    private suspend fun callUpdateFewContactEndpoint(
        listContacts: List<ContactEntity>,
        coroutineDispatcher: CoroutineDispatcher
    ) {
        withContext(coroutineDispatcher) {
            thanksApi.updateFewContact(listContacts)
                .enqueue(object : Callback<UpdateFewContactsResponse> {
                    override fun onResponse(
                        call: Call<UpdateFewContactsResponse>,
                        response: Response<UpdateFewContactsResponse>
                    ) {
                        _isLoading.postValue(false)
                        if (response.code() == 200) {
                            _updateFewContact.postValue(response.body())
                        } else {
                            _updateFewContactError.postValue(response.message() + " " + response.code())
                        }
                    }

                    override fun onFailure(call: Call<UpdateFewContactsResponse>, t: Throwable) {
                        _isLoading.postValue(false)
                        _updateFewContactError.postValue(t.message)
                    }
                })
        }
    }

    fun loadUpdateAvatarUserProfile(
        userIdInner: String,
        filePath: String,
        filePathCropped: String
    ) {
        _isLoadingUpdateAvatar.postValue(true)
        viewModelScope.launch {
            val userId = userDataRepository.getProfileId()
            if (userId != null)
                withContext(Dispatchers.IO) {
                    when (val result = profileRepository.updateUserAvatar(userIdInner, filePath, filePathCropped)) {
                        is ResultWrapper.Success -> {
                            _isSuccessfulOperation.postValue(MessageAboutOperation(true, "Успешно"))
                            userDataRepository.saveUserAvatar(result.value.photo)
                        }
                        else -> {
                            if (result is ResultWrapper.GenericError) {
                                _profileError.postValue(result.error + " " + result.code)
                                _isSuccessfulOperation.postValue(result.error?.let {
                                    MessageAboutOperation(false,
                                        it
                                    )
                                })


                            } else if (result is ResultWrapper.NetworkError) {
                                _profileError.postValue("Ошибка сети")
                                _isSuccessfulOperation.postValue(MessageAboutOperation(false, "Ошибка сети"))

                            }
                        }
                    }
                    _isLoadingUpdateAvatar.postValue(false)
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


}