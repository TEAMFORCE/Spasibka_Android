package com.teamforce.thanksapp.domain.repositories

import androidx.paging.PagingData
import com.teamforce.thanksapp.data.entities.Stickers.StickerEntity
import com.teamforce.thanksapp.data.response.CancelTransactionResponse
import com.teamforce.thanksapp.data.response.GetCommentsResponse
import com.teamforce.thanksapp.data.response.GetReactionsResponse
import com.teamforce.thanksapp.data.response.LikeResponse
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.models.reactions.LikeResponseModel
import com.teamforce.thanksapp.model.domain.CommentModel
import com.teamforce.thanksapp.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface ReactionRepository {
    suspend fun pressLike(
        likedObjectId: Int,
        typeOfObject: ObjectsToLike,
        position: Int? = null
    ): ResultWrapper<LikeResponseModel>?

    fun getReactions(
        objectId: Int,
        typeOfObject: ObjectsToLike
    ): Flow<PagingData<GetReactionsResponse.InnerInfoLike>>

    fun getComments(
        objectId: Int,
        typeOfObject: ObjectsComment
    ): Flow<PagingData<CommentModel>>

    fun getLastThreeComments(
        objectId: Int,
        typeOfObject: ObjectsComment
    ): Flow<PagingData<CommentModel>>

    suspend fun createChallengeComment(
        objectId: Int,
        typeOfObject: ObjectsComment,
        text: String,
        gifUrl: String?,
        picture: String?
    ): ResultWrapper<CancelTransactionResponse>

    suspend fun deleteComment(
        commentId: Int
    ): ResultWrapper<CancelTransactionResponse>

    suspend fun getAllStickers(): ResultWrapper<List<StickerEntity>>
}