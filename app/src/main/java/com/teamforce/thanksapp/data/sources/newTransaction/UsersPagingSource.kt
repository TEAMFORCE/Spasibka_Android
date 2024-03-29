package com.teamforce.thanksapp.data.sources.newTransaction

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.request.UserListWithoutInputRequest
import com.teamforce.thanksapp.data.request.UsersListRequest
import com.teamforce.thanksapp.data.response.UserListItem.UserBean
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException

class UsersPagingSource (
    private val api: ThanksApi,
    private val input: String = ""
) : PagingSource<Int, UserBean>() {

    override fun getRefreshKey(state: PagingState<Int, UserBean>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserBean> {
        var pageIndex = params.key ?: 1

        if (params is LoadParams.Refresh) {
            pageIndex = 1
        }

        return try {
            val response = if(input.trim().isEmpty()){
                api.getUsersWithoutInput(
                    UserListWithoutInputRequest(
                        limit = Consts.PAGE_SIZE,
                        offset = pageIndex,
                    )
                )
            }else{
                api.getUsersList(
                    UsersListRequest(
                        data = input,
                        limit = Consts.PAGE_SIZE,
                        offset = pageIndex,
                    )
                )
            }
            val nextKey =
                if (response.isEmpty()) {
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