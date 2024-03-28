package com.teamforce.thanksapp.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.Stickers.StickerEntity
import com.teamforce.thanksapp.data.request.CreateCommentRequest
import com.teamforce.thanksapp.data.response.CancelTransactionResponse
import com.teamforce.thanksapp.data.response.GetReactionsResponse
import com.teamforce.thanksapp.data.response.LikeResponse
import com.teamforce.thanksapp.data.sources.reactions.CommentsPagingSource
import com.teamforce.thanksapp.data.sources.reactions.ReactionsPagingSource
import com.teamforce.thanksapp.domain.mappers.reactions.ReactionsMapper
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.domain.models.reactions.LikeResponseModel
import com.teamforce.thanksapp.domain.repositories.ReactionRepository
import com.teamforce.thanksapp.model.domain.CommentModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.isNullOrEmptyMy
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileNotFoundException
import java.text.FieldPosition
import javax.inject.Inject

class ReactionRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val reactionsMapper: ReactionsMapper
) : ReactionRepository {

    override suspend fun pressLike(
        likedObjectId: Int,
        typeOfObject: ObjectsToLike,
        position: Int?
    ): ResultWrapper<LikeResponseModel>? {
        val data = when (typeOfObject) {
            ObjectsToLike.BIRTHDAY -> return null
            else -> mapOf("like_kind" to 1, typeOfObject.idForLike to likedObjectId)
        }
        return safeApiCall(Dispatchers.IO) {
            reactionsMapper.mapLikeResponse(thanksApi.pressLike(data), position)
        }
    }

    override fun getReactions(
        objectId: Int,
        typeOfObject: ObjectsToLike
    ): Flow<PagingData<GetReactionsResponse.InnerInfoLike>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                ReactionsPagingSource(
                    api = thanksApi,
                    objectId = objectId,
                    typeOfObject = typeOfObject
                )
            }
        ).flow
    }

    override fun getComments(
        objectId: Int,
        typeOfObject: ObjectsComment
    ): Flow<PagingData<CommentModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                CommentsPagingSource(
                    api = thanksApi,
                    objectId = objectId,
                    pageSize = Consts.PAGE_SIZE,
                    isReversedComment = false,
                    onlyFirstPage = false,
                    typeOfObject = typeOfObject
                )
            }
        ).flow
    }

    override fun getLastThreeComments(
        objectId: Int,
        typeOfObject: ObjectsComment
    ): Flow<PagingData<CommentModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                CommentsPagingSource(
                    api = thanksApi,
                    objectId = objectId,
                    pageSize = 3,
                    onlyFirstPage = true,
                    isReversedComment = true,
                    typeOfObject = typeOfObject
                )
            }
        ).flow
    }

    override suspend fun createChallengeComment(
        objectId: Int,
        typeOfObject: ObjectsComment,
        text: String,
        gifUrl: String?,
        picture: String?,
    ): ResultWrapper<CancelTransactionResponse> {
        var body: MultipartBody.Part? = null
        var gifUrlInner: String? = gifUrl
        if (!gifUrl.isNullOrEmptyMy() && gifUrl?.contains("/data/") == true) {
            try {
                val file = File(gifUrl)
                val requestFile: RequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                body = MultipartBody.Part.createFormData("picture", file.name, requestFile)
                gifUrlInner = null
            } catch (e: FileNotFoundException) {
                Log.e(TAG, e.message ?: "Error")
            }
        }
        val data = CreateCommentRequest(text = text, gif = gifUrlInner, picture = body)

        when (typeOfObject) {
            ObjectsComment.CHALLENGE -> {
                data.challenge_id = objectId
            }
            ObjectsComment.REPORT_OF_CHALLENGE -> {
                data.challenge_report_id = objectId
            }
            ObjectsComment.TRANSACTION -> {
                data.transaction_id = objectId
            }
            ObjectsComment.OFFER -> {
                data.offer_id = objectId
            }
        }
        return safeApiCall(Dispatchers.IO) {
            thanksApi.createComment(
                picture = data.picture,
                challengeId = data.challenge_id,
                challengeReportId = data.challenge_report_id,
                offerId = data.offer_id,
                transactionId = data.transaction_id,
                gif = data.gif,
                text = data.text
            )
        }
    }

    override suspend fun deleteComment(commentId: Int): ResultWrapper<CancelTransactionResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.deleteComment(commentId)
        }
    }

    override suspend fun getAllStickers(): ResultWrapper<List<StickerEntity>> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getAllStickers()
        }
    }

    companion object {
        private const val TAG = "ReactionRepositoryImpl"
    }

}