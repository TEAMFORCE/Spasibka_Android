package com.teamforce.thanksapp.presentation.viewmodel.reactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.teamforce.thanksapp.data.response.GetReactionsResponse
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.repositories.ReactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ReactionsViewModel @Inject constructor(
    private val reactionRepository: ReactionRepository
): ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _reactions = MutableLiveData<GetReactionsResponse.InnerInfoLike?>()
    val reactions: LiveData<GetReactionsResponse.InnerInfoLike?> = _reactions
    private val _reactionsLoadingError = MutableLiveData<String>()
    val reactionsLoadingError: LiveData<String> = _reactionsLoadingError


    fun loadReactions(
        objectId: Int,
        typeOfObject: ObjectsToLike
    ): Flow<PagingData<GetReactionsResponse.InnerInfoLike>> {
        return reactionRepository.getReactions(
            objectId = objectId,
            typeOfObject = typeOfObject
        ).map { it }
    }
}