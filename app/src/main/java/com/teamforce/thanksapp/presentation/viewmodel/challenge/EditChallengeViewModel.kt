package com.teamforce.thanksapp.presentation.viewmodel.challenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.response.commons.CommonStatusResponse
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.CreateChallengeSettingsModel
import com.teamforce.thanksapp.domain.models.challenge.updateChallenge.UpdateChallengeModel
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class EditChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val userDataRepository: com.teamforce.thanksapp.utils.UserDataRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _updatedChallengeResponse = MutableLiveData<CommonStatusResponse?>()
    val updatedChallengeResponse: LiveData<CommonStatusResponse?> = _updatedChallengeResponse

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    fun amIAdmin() = userDataRepository.userIsAdmin()


    fun updateChallenge(
        challengeId: Int,
        dataOfChallenge: UpdateChallengeModel,
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = challengeRepository.updateChallenge(
                    challengeId,
                    dataOfChallenge
                )) {
                    is ResultWrapper.Success -> {
                        _internetError.postValue(false)
                        _updatedChallengeResponse.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _updatedChallengeResponse.postValue(
                                CommonStatusResponse(
                                result.code ?: -1, result.error
                            )
                            )
                            _internetError.postValue(false)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    private val defaultSettingsChallenge = CreateChallengeSettingsModel(
        listOf(),
        multipleReports = true,
        showContenders = false,
        challengeWithVoting = false
    )



    fun getChallengeSettings(): CreateChallengeSettingsModel{
        return if(_challengeSettingsState.value == null){
            defaultSettingsChallenge
        }else{
            _challengeSettingsState.value!!
        }
    }


    fun updateChallengeSettings(updateFunction: (CreateChallengeSettingsModel) -> CreateChallengeSettingsModel) {
        val currentSettings = _challengeSettingsState.value
        if (currentSettings != null) {
            val updatedSettings = updateFunction(currentSettings)
            _challengeSettingsState.value = updatedSettings
        }
    }

    private val _challengeSettingsState = MutableLiveData(defaultSettingsChallenge)
    val challengeSettingsState: LiveData<CreateChallengeSettingsModel> = _challengeSettingsState


    fun getCreateChallengeSettingsFromBack(
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val result = challengeRepository.getCreateChallengeSettings()) {
                    is ResultWrapper.Success -> {
                        _challengeSettingsState.postValue(result.value)
                    }
                    else -> {
                        _internetError.postValue(true)
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    private val _deleteChallengeStatus = MutableLiveData<CommonStatusResponse?>()
    val deleteChallengeStatus: LiveData<CommonStatusResponse?> = _deleteChallengeStatus

    private val _deleteChallengeStatusError = MutableLiveData<String>()
    val deleteChallengeStatusError: LiveData<String> = _deleteChallengeStatusError

    fun deleteChallenge(
        challengeId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = challengeRepository.deleteChallenge(challengeId)) {
                    is ResultWrapper.Success -> {
                        _deleteChallengeStatus.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _deleteChallengeStatusError.postValue(
                                result.error + " " + result.code
                            )

                        } else if (result is ResultWrapper.NetworkError) {
                            // _deleteChallengeStatus.postValue(result)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }
}