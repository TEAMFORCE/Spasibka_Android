package com.teamforce.thanksapp.data.sources.challenge

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.domain.mappers.challenges.ChainMapper
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChallengeChainsModel
import com.teamforce.thanksapp.domain.models.recommendations.RecommendModel
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException

class ChallengeChainsPagingSource(
    private val api: ThanksApi,
    private val organizationId: String?,
    private val chainMapper: ChainMapper,
    private val chainState: ChainState?
) : PagingSource<Int, ChallengeChainsModel>() {

    override fun getRefreshKey(state: PagingState<Int, ChallengeChainsModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChallengeChainsModel> {
        var pageIndex = params.key ?: 1

        if (params is LoadParams.Refresh) {
            pageIndex = 1
        }

        return try {
            val response = api.getChallengeChains(
                organizationId = organizationId ?: "0",
                limit = Consts.PAGE_SIZE,
                offset = pageIndex,
                state = chainState?.designation
            )
            val nextKey =
                if (response.data.isEmpty()) {
                    null
                } else {
                    pageIndex + (params.loadSize / Consts.PAGE_SIZE)
                }
            LoadResult.Page(
                data = chainMapper.mapChallengeChains(response.data),
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
/**
 * Класс представляет список состояний цепочек челленджей
 * @param designation char обозначения состояний A - Active, D - Deffered, C - Closed
 */
enum class ChainState(val designation: Char){
    ACTIVE('A'), DEFFERED('D'), CLOSED('C');
    companion object {

        /**
         * @param char обозначения состояний A - Active, D - Deffered, C - Closed
         * @return Вернет ChainState, при ошибке IllegalArgumentException вернет Active
         */
        fun safetyValueOf(char: Char): ChainState {
            return try {
                ChainState.valueOf(char.uppercase())
            } catch (e: IllegalArgumentException) {
                Log.e("enum ChainState", "IllegalArgumentException")
                ACTIVE
            }
        }
    }
}