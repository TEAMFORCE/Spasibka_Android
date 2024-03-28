package com.teamforce.thanksapp.data.sources.challenge

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.domain.mappers.challenges.ChainMapper
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ParticipantChainModel
import com.teamforce.thanksapp.model.domain.ChallengeModel
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException

class ParticipantChainPagingSource (
    private val api: ThanksApi,
    private val organizationId: String?,
    private val chainId: Long? = null,
    private val chainMapper: ChainMapper,
) : PagingSource<Int, ParticipantChainModel>() {

    override fun getRefreshKey(state: PagingState<Int, ParticipantChainModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ParticipantChainModel> {
        var pageIndex = params.key ?: 1

        if (params is LoadParams.Refresh) {
            pageIndex = 1
        }

        return try {
            val response = api.getChainParticipants(
                limit = Consts.PAGE_SIZE,
                offset = pageIndex,
                chainId = chainId ?: 0L,
                organizationId = organizationId ?: "0"
            )
            val nextKey =
                if (response.data.isEmpty()) {
                    null
                } else {
                    pageIndex + (params.loadSize / Consts.PAGE_SIZE)
                }
            LoadResult.Page(
                data = chainMapper.mapListParticipants(response.data),
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