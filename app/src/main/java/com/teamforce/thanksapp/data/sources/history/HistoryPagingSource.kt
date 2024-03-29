package com.teamforce.thanksapp.data.sources.history

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.response.HistoryItem
import com.teamforce.thanksapp.domain.mappers.history.HistoryMapper
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.domain.models.history.UserTransactionsGroupModel
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException

class HistoryPagingSource(
    private val api: ThanksApi,
    private val sentOnly: Int?,
    private val receivedOnly: Int?,
    private val fromDate: String?,
    private val upDate: String?,
    private val mapper: HistoryMapper,
    private val showOnlyThreeElement: Boolean = false
) : PagingSource<Int, HistoryItemModel.UserTransactionsModel>() {

    override fun getRefreshKey(state: PagingState<Int, HistoryItemModel.UserTransactionsModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HistoryItemModel.UserTransactionsModel> {
        var pageIndex = params.key ?: 1

        if (params is LoadParams.Refresh) {
            pageIndex = 1
        }

        return try {
            val response = mapper.mapList(
                api.getUserTransactions(
                    limit = if(showOnlyThreeElement) 3 else  Consts.PAGE_SIZE,
                    offset = pageIndex,
                    sentOnly = sentOnly,
                    receivedOnly = receivedOnly,
                    fromDate = fromDate,
                    upDate = upDate
                )
            )

            val nextKey =
                if (response.isEmpty() || showOnlyThreeElement) {
                    null
                } else {
                    pageIndex + (params.loadSize / Consts.PAGE_SIZE)
                }
            LoadResult.Page(
                data = response,
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