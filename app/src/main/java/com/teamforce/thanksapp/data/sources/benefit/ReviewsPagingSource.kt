package com.teamforce.thanksapp.data.sources.benefit

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.domain.mappers.benefit.ReviewMapper
import com.teamforce.thanksapp.domain.models.benefit.ReviewModel
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException


class ReviewsPagingSource(
    private val api: ThanksApi,
    private val reviewsMapper: ReviewMapper,
    private val marketId: Int,
    private val offerId: Long,
    private val reverseOutput: Boolean,
    private val orderBy: String?
) : PagingSource<Int, ReviewModel>() {

    override fun getRefreshKey(state: PagingState<Int, ReviewModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ReviewModel> {
        var pageIndex = params.key ?: 1

        if (params is LoadParams.Refresh) {
            pageIndex = 1
        }

        return try {
            val response = api.getReviews(
                marketId = marketId,
                offerId = offerId,
                limit = Consts.PAGE_SIZE,
                offset = pageIndex,
                orderBy = orderBy,
                reverseOutput = if(reverseOutput) 1 else 0
            )
            val nextKey =
                if (response.data.isEmpty()) null
                else {
                    pageIndex + (params.loadSize / Consts.PAGE_SIZE)
                }
            LoadResult.Page(
                data = reviewsMapper.mapReviewsList(response.data),
                prevKey = if (pageIndex == 1) null else pageIndex,
                nextKey = nextKey
            )

        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}