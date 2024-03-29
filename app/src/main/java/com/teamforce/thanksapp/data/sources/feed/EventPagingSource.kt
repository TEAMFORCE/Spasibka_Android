package com.teamforce.thanksapp.data.sources.feed

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.domain.mappers.feed.FeedMapper
import com.teamforce.thanksapp.domain.models.events.EventDataModel
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException

class EventPagingSource(
    private val api: ThanksApi,
    private val feedMapper: FeedMapper,
    private val filters: List<Int>?
) : PagingSource<Int, EventDataModel>() {
    override fun getRefreshKey(state: PagingState<Int, EventDataModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, EventDataModel> {
        var pageIndex = params.key ?: 1

        if (params is LoadParams.Refresh) {
            pageIndex = 1
        }

        return try {
            val response = api.getEventsNew(
                limit = Consts.PAGE_SIZE,
                offset = pageIndex,
                filters = filters
            )
            val nextKey =
                if (response.data.isEmpty()) null
                else {
                    pageIndex + (params.loadSize / Consts.PAGE_SIZE)
                }
            LoadResult.Page(
                data = feedMapper.mapListNewFeed(response.data),
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