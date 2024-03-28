package com.teamforce.thanksapp.presentation.viewmodel.challenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.CreateChallengeSettingsModel
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.model.domain.ChallengeModel
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.isNotNullOrEmptyMy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class CreateChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val userDataRepository: com.teamforce.thanksapp.utils.UserDataRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _createChallenge = MutableLiveData<ChallengeModel?>()
    val createChallenge: LiveData<ChallengeModel?> = _createChallenge
    private val _createChallengeError = MutableLiveData<String?>()
    val createChallengeError: LiveData<String?> = _createChallengeError
    private val _isSuccessOperation = MutableLiveData<Boolean>()
    val challengeSuccessfulCreated: LiveData<Boolean> = _isSuccessOperation

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    private val defaultSettingsChallenge = CreateChallengeSettingsModel(
        listOf(),
        multipleReports = true,
        showContenders = false,
        challengeWithVoting = false
    )

    private val _challengeSettingsState = MutableLiveData(defaultSettingsChallenge)
    val challengeSettingsState: LiveData<CreateChallengeSettingsModel> = _challengeSettingsState

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


    fun createChallenge(
        name: String,
        description: String,
        endAt: ZonedDateTime?,
        startAt: ZonedDateTime?,
        amountFund: Int,
        parameter_id: Int,
        parameter_value: Int,
        templateId: Int? = null,
        photos: List<MultipartBody.Part>,
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = challengeRepository.createChallenge(
                    name = name,
                    description = description,
                    endAt = endAt,
                    startAt = startAt,
                    amountFund = amountFund,
                    parameter_id = parameter_id,
                    parameter_value = parameter_value,
                    templateId = templateId,
                    challengeType = _challengeSettingsState.value?.challengeWithVoting ?: defaultSettingsChallenge.challengeWithVoting,
                    severalReports = _challengeSettingsState.value?.multipleReports ?: defaultSettingsChallenge.challengeWithVoting,
                    showContenders = _challengeSettingsState.value?.showContenders ?: defaultSettingsChallenge.challengeWithVoting,
                    debitAccountId = _challengeSettingsState.value?.accounts?.find { it.current }?.id,
                    photos = photos,
                )) {
                    is ResultWrapper.Success -> {
                        _internetError.postValue(false)
                        _isSuccessOperation.postValue(true)
                        _createChallenge.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _internetError.postValue(false)
                            _isSuccessOperation.postValue(false)
                            _createChallengeError.postValue(result.error)

                        } else if (result is ResultWrapper.NetworkError) {
                            _internetError.postValue(true)
                            _isSuccessOperation.postValue(false)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun parseZonedDateTime(dateTimeString: String?): ZonedDateTime? {
        if (dateTimeString.isNotNullOrEmptyMy()) {
            return try {
                ZonedDateTime.parse(dateTimeString)
            } catch (e: DateTimeParseException) {
                null
            }
        }
        return null
    }


    data class DataForCreateChallenge(
        var name: String,
        var description: String,
        var endAt: ZonedDateTime? = null,
        var startAt: ZonedDateTime? = null,
        var amountFund: Int,
        var parameter_id: Int,
        var parameter_value: Int,
        var templateId: Int? = null
    )
}