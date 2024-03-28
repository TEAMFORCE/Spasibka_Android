package com.teamforce.thanksapp.presentation.fragment.eventScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.teamforce.thanksapp.domain.interactors.eventScreen.EventsInteractor
import com.teamforce.thanksapp.domain.models.events.EventFilterModel
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.models.reactions.LikeResponseModel
import com.teamforce.thanksapp.domain.repositories.FeedRepository
import com.teamforce.thanksapp.domain.repositories.ReactionRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.SingleLiveEvent
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsInteractor: EventsInteractor,
    private val reactionRepository: ReactionRepository
): ViewModel(){

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError


    private val _filtersIds = MutableLiveData<Set<Int>?>()
    val filtersIds: LiveData<Set<Int>?> = _filtersIds

    private val _saveFilterStatus = MutableLiveData<String?>()
    val saveFilterStatus: LiveData<String?> = _saveFilterStatus


    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error



    fun saveFilters() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _isLoading.postValue(true)
                when (val result = eventsInteractor.saveFilters(_filtersIds.value)) {
                    is ResultWrapper.Success -> {
                        _saveFilterStatus.postValue(result.value.status)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _saveFilterStatus.postValue(result.error)

                        } else if (result is ResultWrapper.NetworkError) {

                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }



    fun updateFilters(filterSet: Set<Int>) {
        _filtersIds.value = filterSet
    }

    val filters: Flow<EventFilterModel?> = flow {
        _isLoading.postValue(true)

        when (val result = eventsInteractor.getFilters()) {
            is ResultWrapper.Success -> {
                emit(result.value)
            }
            else -> {
                if (result is ResultWrapper.GenericError) {
                    // Обработка ошибок, если нужно
                    _error.postValue(result.error)
                } else if (result is ResultWrapper.NetworkError) {
                    // Обработка ошибок сети, если нужно
                }
            }
        }

        _isLoading.postValue(false)
    }.flowOn(Dispatchers.IO)

    private val coroutineScope =
        CoroutineScope(CoroutineExceptionHandler { _, exception ->
            _error.postValue(exception.localizedMessage)
            Timber.e(exception)
        } + Dispatchers.IO)

    fun getEvents() = eventsInteractor.getEvents(filtersIds.value).stateIn(
        scope =  coroutineScope,
        started = SharingStarted.WhileSubscribed(5000, replayExpirationMillis = 1000),
        initialValue = PagingData.empty()
    ).cachedIn(coroutineScope)


    private val _likeResult =  SingleLiveEvent<LikeResponseModel?>()
    val likeResult: SingleLiveEvent<LikeResponseModel?> =  _likeResult
    private val _likeError = MutableLiveData<String>()
    val likeError: LiveData<String> = _likeError

    fun pressLike(
        objectId: Int,
        typeOfObject: ObjectsToLike,
        position: Int
    ) {

        viewModelScope.launch {
            reactionRepository.pressLike(objectId, typeOfObject, position)?.toResultState(
                onSuccess = {
                    _likeResult.postValue(it)
                },
                onError = { error, code ->
                    _likeError.postValue("$error $code")
                },
                onLoading = {

                }
            )
        }
    }
}