package com.teamforce.thanksapp.presentation.viewmodel.challenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.teamforce.thanksapp.data.response.CancelTransactionResponse
import com.teamforce.thanksapp.data.response.GetCommentsResponse
import com.teamforce.thanksapp.data.response.LikeResponse
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.models.reactions.LikeResponseModel
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.domain.repositories.ReactionRepository
import com.teamforce.thanksapp.model.domain.CommentModel
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val reactionRepository: ReactionRepository,
    private val userDataRepository: com.teamforce.thanksapp.utils.UserDataRepository

) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _comments = MutableLiveData<GetCommentsResponse?>()
    val comments: LiveData<GetCommentsResponse?> = _comments
    private val _commentsLoadingError = MutableLiveData<String>()
    val commentsLoadingError: LiveData<String> = _commentsLoadingError


    private val _deleteComment = MutableLiveData<CancelTransactionResponse?>()
    val deleteComment: LiveData<CancelTransactionResponse?> = _deleteComment


    private val _createComments = MutableLiveData<String>()
    val createComments: LiveData<String> = _createComments


    fun getProfileId() = userDataRepository.getProfileId()


    private val coroutineScope =
        CoroutineScope(CoroutineExceptionHandler { _, exception ->
            Timber.e(exception)
            _commentsLoadingError.postValue(exception.localizedMessage)
        } + Dispatchers.IO)

    fun loadComments(
        objectId: Int,
        typeOfObject: ObjectsComment
    ): Flow<PagingData<CommentModel>> = reactionRepository.getComments(
        objectId,
        typeOfObject
    ).stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    ).cachedIn(coroutineScope).map { it }


    fun loadLastThreeComments(
        objectId: Int,
        typeOfObject: ObjectsComment
    ): Flow<PagingData<CommentModel>> {
        return reactionRepository.getLastThreeComments(
            objectId,
            typeOfObject
        ).stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        ).cachedIn(coroutineScope).map { it }
    }

//    fun loadComments(
//        challengeId: Int
//    ) {
//        _createCommentsLoading.postValue(true)
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                when (val result = challengeRepository.loadChallengeComments(challengeId)) {
//                    is ResultWrapper.Success -> {
//                        _comments.postValue(result.value)
//                    }
//                    else -> {
//                        if (result is ResultWrapper.GenericError) {
//                            _commentsLoadingError.postValue(result.error + " " + result.code)
//
//                        } else if (result is ResultWrapper.NetworkError) {
//                            _commentsLoadingError.postValue("Ошибка сети")
//                        }
//                    }
//                }
//                _isLoading.postValue(false)
//            }
//        }
//    }

    fun createComment(
        objectId: Int,
        objectsComment: ObjectsComment,
        text: String,
        gifUrl: String? = null,
        picture: String? = null,
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val result = reactionRepository.createChallengeComment(
                    objectId,
                    objectsComment,
                    text,
                    gifUrl,
                    picture
                )) {
                    is ResultWrapper.Success -> {
                        _createComments.postValue("")
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _commentsLoadingError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _commentsLoadingError.postValue(result.error)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun deleteComment(
        commentId: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val result = reactionRepository.deleteComment(commentId)) {
                    is ResultWrapper.Success -> {
                        _deleteComment.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _commentsLoadingError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _commentsLoadingError.postValue(result.error)
                        }
                    }
                }
                _isLoading.postValue(false)
            }
        }
    }


    private val _likeResult =  SingleLiveEvent<LikeResponseModel?>()
    val likeResult: SingleLiveEvent<LikeResponseModel?> =  _likeResult
    private val _likeError = MutableLiveData<String>()
    val likeError: LiveData<String> = _likeError



    fun pressLike(
        offerId: Int,
        position: Int
    ) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val result = reactionRepository.pressLike(offerId, ObjectsToLike.COMMENT, position)) {
                    is ResultWrapper.Success -> {
                        _likeResult.postValue(result.value)
                    }
                    else -> {
                        if (result is ResultWrapper.GenericError) {
                            _likeError.postValue(result.error + " " + result.code)

                        } else if (result is ResultWrapper.NetworkError) {
                            _likeError.postValue("Ошибка сети")
                        }
                    }
                }
            }
        }
    }


}