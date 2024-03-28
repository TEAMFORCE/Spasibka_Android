package com.teamforce.thanksapp.data.sources.reactions

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.request.GetCommentsRequest
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.model.domain.CommentModel
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException

class CommentsPagingSource(
    private val api: ThanksApi,
    private val objectId: Int,
    private val pageSize: Int,
    private val onlyFirstPage: Boolean,
    private val isReversedComment: Boolean = true,
    private val typeOfObject: ObjectsComment
) : PagingSource<Int, CommentModel>() {

    override fun getRefreshKey(state: PagingState<Int, CommentModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CommentModel> {
        var pageIndex = params.key ?: 0

        if (params is LoadParams.Refresh) {
            pageIndex = 0
        }

        return try {
            val response = when (typeOfObject) {
                ObjectsComment.CHALLENGE -> {
                    api.getComments(
                        GetCommentsRequest(
                            challenge_id = objectId,
                            limit = pageSize,
                            isReversed = isReversedComment,
                            offset = pageIndex
                        )
                    )
                }
                ObjectsComment.REPORT_OF_CHALLENGE -> {
                    api.getComments(
                        GetCommentsRequest(
                            challenge_report_id = objectId,
                            limit = pageSize,
                            isReversed = isReversedComment,
                            offset = pageIndex
                        )
                    )
                }
                ObjectsComment.TRANSACTION -> {
                    api.getComments(
                        GetCommentsRequest(
                            transaction_id = objectId,
                            limit = pageSize,
                            isReversed = isReversedComment,
                            offset = pageIndex
                        )
                    )
                }
                ObjectsComment.OFFER -> {
                    api.getComments(
                        GetCommentsRequest(
                            offerId = objectId,
                            limit = pageSize,
                            isReversed = isReversedComment,
                            offset = pageIndex
                        )
                    )
                }
            }
            val nextKey: Int? = if (onlyFirstPage || response.comments.isEmpty()) null else pageIndex + (pageSize)
            // Переворот массива, чтобы самый свежий коммент был снизу при isReversed = true
            if(isReversedComment == true){
                LoadResult.Page(
                    data = response.comments.reversed(),
                    prevKey = if (pageIndex == 0) null else pageIndex,
                    nextKey = nextKey
                )
            }else{
                LoadResult.Page(
                    data = response.comments,
                    prevKey = if (pageIndex == 0) null else pageIndex,
                    nextKey = nextKey
                )
            }

        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

}