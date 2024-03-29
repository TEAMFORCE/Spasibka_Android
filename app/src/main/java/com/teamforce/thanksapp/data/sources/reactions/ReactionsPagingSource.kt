package com.teamforce.thanksapp.data.sources.reactions

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.request.GetReactionsRequest
import com.teamforce.thanksapp.data.response.GetReactionsResponse
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException

class ReactionsPagingSource(
    private val api: ThanksApi,
    private val objectId: Int,
    private val typeOfObject: ObjectsToLike
) : PagingSource<Int, GetReactionsResponse.InnerInfoLike>() {

    override fun getRefreshKey(state: PagingState<Int, GetReactionsResponse.InnerInfoLike>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GetReactionsResponse.InnerInfoLike> {
        var pageIndex = params.key ?: 0

//        if (params is LoadParams.Refresh) {
//            pageIndex = 1
//        }

        return try {
            val response = when(typeOfObject){
                ObjectsToLike.CHALLENGE -> {
                    api.getReactions(
                        GetReactionsRequest(
                            challenge_id = objectId,
                            limit = Consts.PAGE_SIZE,
                            offset = pageIndex
                        )
                    )
                }
                ObjectsToLike.REPORT_OF_CHALLENGE -> {
                    api.getReactions(
                        GetReactionsRequest(
                            challenge_report_id = objectId,
                            limit = Consts.PAGE_SIZE,
                            offset = pageIndex
                        )
                    )

                }
                ObjectsToLike.TRANSACTION -> {
                    api.getReactions(
                        GetReactionsRequest(
                            transaction_id = objectId,
                            limit = Consts.PAGE_SIZE,
                            offset = pageIndex
                        )
                    )
                }
                ObjectsToLike.OFFER -> {
                    api.getReactions(
                        GetReactionsRequest(
                            offer_id = objectId,
                            limit = Consts.PAGE_SIZE,
                            offset = pageIndex
                        )
                    )
                }
                ObjectsToLike.PURCHASE -> {
                    api.getReactions(
                        GetReactionsRequest(
                            order_id = objectId,
                            limit = Consts.PAGE_SIZE,
                            offset = pageIndex
                        )
                    )
                }
                ObjectsToLike.BIRTHDAY -> {
                    api.getReactions(
                        GetReactionsRequest(
                            transaction_id = objectId,
                            limit = Consts.PAGE_SIZE,
                            offset = pageIndex
                        )
                    )
                }
                ObjectsToLike.COMMENT -> {
                    api.getReactions(
                        GetReactionsRequest(
                            comment_id = objectId,
                            limit = Consts.PAGE_SIZE,
                            offset = pageIndex
                        )
                    )
                }
                ObjectsToLike.OFFER_REVIEW -> {
                    api.getReactions(
                        GetReactionsRequest(
                            offer_review_id = objectId,
                            limit = Consts.PAGE_SIZE,
                            offset = pageIndex
                        )
                    )
                }
            }
            val nextKey =
                if (response.likes[0].items.isEmpty()) {
                    null
                } else {
                    pageIndex + (Consts.PAGE_SIZE)
                }
            LoadResult.Page(
                data = response.likes[0].items,
                prevKey = if (pageIndex == 0) null else pageIndex,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

}