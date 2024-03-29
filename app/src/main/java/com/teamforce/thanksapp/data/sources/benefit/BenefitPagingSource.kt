package com.teamforce.thanksapp.data.sources.benefit

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.domain.mappers.benefit.BenefitMapper
import com.teamforce.thanksapp.domain.mappers.feed.FeedMapper
import com.teamforce.thanksapp.domain.models.benefit.BenefitModel
import com.teamforce.thanksapp.domain.models.feed.FeedModel
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException

class BenefitPagingSource(
    private val api: ThanksApi,
    private val benefitMapper: BenefitMapper,
    private val idCategory: Int,
    private val marketId: Int,
    private val containString: String?,
    private val minPrice: String?,
    private val maxPrice: String?
) : PagingSource<Int, BenefitModel>() {

    override fun getRefreshKey(state: PagingState<Int, BenefitModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BenefitModel> {
        var pageIndex = params.key ?: 1

        if (params is LoadParams.Refresh) {
            pageIndex = 1
        }

        return try {
            val response = api.getOffers(
                marketId = marketId,
                limit = Consts.PAGE_SIZE,
                offset = pageIndex,
                category = if (idCategory == 0) null else idCategory,
                contain = containString,
                minPrice = if (minPrice == "") null else minPrice,
                maxPrice = if (maxPrice == "") null else maxPrice
            )
            val nextKey =
                if (response.isEmpty()) null
                else {
                    pageIndex + (params.loadSize / Consts.PAGE_SIZE)
                }
            LoadResult.Page(
                data = benefitMapper.mapList(response),
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
