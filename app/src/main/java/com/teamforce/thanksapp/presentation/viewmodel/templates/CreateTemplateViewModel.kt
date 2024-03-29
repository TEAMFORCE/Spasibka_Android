package com.teamforce.thanksapp.presentation.viewmodel.templates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.api.ImageFileData
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.repositories.TemplatesRepository
import com.teamforce.thanksapp.domain.usecases.LoadProfileUseCase
import com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge.SettingsChallengeFragment
import com.teamforce.thanksapp.presentation.fragment.templates.createTemplate.SettingsTemplateFragment
import com.teamforce.thanksapp.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class CreateTemplateViewModel @Inject constructor(
    private val templatesRepository: TemplatesRepository,
    private val loadProfileUseCase: LoadProfileUseCase
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    private val _isSuccessOperation = MutableLiveData<Boolean>()
    val isSuccessOperation: LiveData<Boolean> = _isSuccessOperation

    private val _createTemplateError = MutableLiveData<String>()
    val createTemplateError: LiveData<String> = _createTemplateError


    private val _profile = MutableLiveData<ProfileModel?>()
    val profile: LiveData<ProfileModel?> = _profile


    fun loadUserProfile() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = loadProfileUseCase()) {
                    is ResultWrapper.Success -> {
                        _profile.postValue(result.value)
                    }
                    is ResultWrapper.GenericError ->{}
                    is ResultWrapper.NetworkError ->
                        _internetError.postValue(true)
                }
                _isLoading.postValue(false)
            }
        }
    }


//    fun saveSettingsForCreateTemplate(
//        challengeWithVoting: Boolean,
//        severalReports: Boolean,
//        showContenders: Boolean,
//        scopeTemplates: ScopeRequestParams
//    ) {
//        val typeOfChallenge = if (challengeWithVoting) ChallengeType.VOTING else ChallengeType.DEFAULT
//        _settingsForCreateTemplate = SettingsForCreateTemplate(
//            typeOfChallenge,
//            severalReports,
//            scopeTemplates,
//            showContenders
//        )
//    }

    fun createTemplate(
        name: String,
        description: String,
        sections: List<Int>,
        photos: List<MultipartBody.Part>,
        settingsForCreateTemplate: SettingsTemplateFragment.SettingsForCreateTemplate
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                _isSuccessOperation.postValue(false)
                when (val result = templatesRepository.createTemplate(
                    scopeTemplates = settingsForCreateTemplate.scopeTemplates.id.toString(),
                    name = name,
                    description = description,
                    photos = photos,
                    challengeType = settingsForCreateTemplate.challengeType.typeOfChallenge,
                    severalReports = changeTypeFromBoolToString(settingsForCreateTemplate.severalReports),
                    showContenders = changeTypeFromBoolToString(settingsForCreateTemplate.showContenders),
                    sections = sections
                )) {
                    is ResultWrapper.Success -> {
                        _isSuccessOperation.postValue(true)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _createTemplateError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)

                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun updateTemplate(
        templateId: Int,
        name: String,
        description: String,
        photos: List<MultipartBody.Part>,
        fileList: List<ImageFileData>? = null,
        sections: List<Int>,
        settingsForCreateTemplate: SettingsTemplateFragment.SettingsForCreateTemplate
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                _isSuccessOperation.postValue(false)
                when (val result = templatesRepository.updateTemplate(
                    templateId = templateId,
                    scopeTemplates = settingsForCreateTemplate.scopeTemplates.id.toString(),
                    name = name,
                    description = description,
                    photos = photos,
                    fileList = fileList,
                    sections = sections,
                    challengeType = settingsForCreateTemplate.challengeType.typeOfChallenge,
                    severalReports = changeTypeFromBoolToString(settingsForCreateTemplate.severalReports),
                    showContenders = changeTypeFromBoolToString(settingsForCreateTemplate.showContenders)
                )) {
                    is ResultWrapper.Success -> {
                        _isSuccessOperation.postValue(true)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _createTemplateError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)

                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    private fun changeTypeFromBoolToString(severalReports: Boolean): String =
        if (severalReports) "yes" else "no"

    private val _screenState = MutableLiveData<ScreenState>()
    val screenState: LiveData<ScreenState> get() = _screenState

    fun setLoading(isLoading: Boolean) {
        val currentState = _screenState.value ?: ScreenState(isLoading = false, updateTemplate = false, errorMessage = null)
        _screenState.value = currentState.copy(isLoading = isLoading)
    }

    fun setUpdateTemplate(isUpdateTemplate: Boolean) {
        val currentState = _screenState.value ?: ScreenState(isLoading = false, updateTemplate = false, errorMessage = null)
        _screenState.value = currentState.copy(updateTemplate = isUpdateTemplate)
    }

    fun setError(errorMessage: String?) {
        val currentState = _screenState.value ?: ScreenState(isLoading = false, updateTemplate = false, errorMessage = null)
        _screenState.value = currentState.copy(errorMessage = errorMessage)
    }

    data class ScreenState(
        val isLoading: Boolean,
        val updateTemplate: Boolean,
        val errorMessage: String?
    )
}