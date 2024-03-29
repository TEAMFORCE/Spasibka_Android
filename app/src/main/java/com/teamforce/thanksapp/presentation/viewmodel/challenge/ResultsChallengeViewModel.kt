package com.teamforce.thanksapp.presentation.viewmodel.challenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ResultsChallengeViewModel @Inject constructor(
) : ViewModel() {

    private var commentPositionForUpdate: Int = 0

    private val _needUpdateComment = MutableLiveData<Boolean>()
    val needUpdateComment: LiveData<Boolean> = _needUpdateComment

    fun setCommentPositionForUpdate(commentPosition: Int){
        commentPositionForUpdate = commentPosition
    }

    fun commentPositionForUpdate() = commentPositionForUpdate

    fun setNeedUpdateCommentState(boolean: Boolean){
        _needUpdateComment.postValue(boolean)
    }

    private val _needUpdateDraft = MutableLiveData<Boolean>()
    val needUpdateDraft: LiveData<Boolean> = _needUpdateDraft

    fun setNeedUpdateDraft(boolean: Boolean){
        _needUpdateDraft.postValue(boolean)
    }

}