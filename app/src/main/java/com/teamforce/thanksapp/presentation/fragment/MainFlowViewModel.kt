package com.teamforce.thanksapp.presentation.fragment

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.admin.GetCurrentPeriodResponse
import com.teamforce.thanksapp.domain.interactors.BrandingInteractors
import com.teamforce.thanksapp.domain.interactors.OnBoardingInteractor
import com.teamforce.thanksapp.domain.interactors.ProfileSettingsInteractor
import com.teamforce.thanksapp.domain.models.branding.OrganizationBrandingModel
import com.teamforce.thanksapp.domain.models.challenge.SectionsOfChallengeType
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.models.profile.ProfileSettingsModel
import com.teamforce.thanksapp.domain.usecases.LoadProfileUseCase
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.isNullOrEmptyMy
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainFlowViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val onBoardingInteractor: OnBoardingInteractor,
    private val brandingInteractors: BrandingInteractors,
    private val loadProfileUseCase: LoadProfileUseCase,
    private val profileSettingsInteractor: ProfileSettingsInteractor
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    val showDialogAboutProfileFields = MutableSharedFlow<Unit>()


    private val _profileSettings = MutableLiveData<ProfileSettingsModel>()
    val profileSettings: LiveData<ProfileSettingsModel> = _profileSettings

    fun loadProfileSettings() {
        viewModelScope.launch {
            profileSettingsInteractor.loadProfileSettings().toResultState(
                onSuccess = {
                    _profileSettings.postValue(it)
                },
                onLoading = {
                    _isLoading.postValue(it)
                },
                onError = { error, code ->
                    _error.postValue(error)
                }
            )
        }
    }


    private val _profile = MutableLiveData<ProfileModel>()
    val profile: LiveData<ProfileModel> = _profile

    fun loadUserProfile() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = loadProfileUseCase()) {
                    is ResultWrapper.Success -> {
                        _profile.postValue(result.value!!)
                        if(profileFieldsEmpty(result.value)) showDialogAboutProfileFields.emit(Unit)
                        userDataRepository.saveProfileId(result.value.profile.id)
                        userDataRepository.saveUsername(result.value.profile.tgName)
                    }
                    is ResultWrapper.GenericError -> {}

                    is ResultWrapper.NetworkError -> {}
                }
                _isLoading.postValue(false)
            }
        }
    }

    private fun profileFieldsEmpty(profile: ProfileModel): Boolean =
        profile.profile.firstname.isNullOrEmptyMy() &&
                profile.profile.surname.isNullOrEmptyMy()

    fun userIsHeadOfDepartment() = userDataRepository.userIsAdmin()

    private val _currentPeriod = MutableLiveData<GetCurrentPeriodResponse?>()
    val currentPeriod: LiveData<GetCurrentPeriodResponse?> = _currentPeriod


    fun loadCurrentPeriod() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = onBoardingInteractor.getCurrentPeriod()) {
                    is ResultWrapper.Success -> {
                        _currentPeriod.postValue(result.value!!)
                    }
                    is ResultWrapper.GenericError -> {
                        _currentPeriod.postValue(null)
                    }

                    is ResultWrapper.NetworkError -> {
                        _currentPeriod.postValue(null)
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    private val _brandingTheme = MutableLiveData<OrganizationBrandingModel?>()
    val brandingTheme: LiveData<OrganizationBrandingModel?> = _brandingTheme

    fun loadRemoteBrandingTheme(
        organizationId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)

                when (val result = brandingInteractors.getRemoteOrganizationBrand(organizationId)) {
                    is ResultWrapper.Success -> {
                        _brandingTheme.postValue(result.value)
                    }
                    is ResultWrapper.GenericError -> {
                        // _profileError.postValue("${result.code}")
                        // _currentPeriod.postValue(null)
                    }

                    is ResultWrapper.NetworkError -> {
                        //_currentPeriod.postValue(null)
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }
    fun getInitChallengeSection(deepLink: Uri?): SectionsOfChallengeType? {
        return deepLink?.pathSegments?.getOrNull(2)?.let { sectionName ->
            runCatching {
                SectionsOfChallengeType.valueOf(sectionName)
            }
                .getOrElse {
                    Timber.tag(TAG).e("Undefined SectionOfChallengeType ${it.localizedMessage}")
                    null
                }
        }
    }

    companion object {
        private const val TAG = "MainFlowViewModel"
    }
}