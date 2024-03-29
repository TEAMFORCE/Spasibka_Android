package com.teamforce.thanksapp.data.sources.templates

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.domain.mappers.templates.TemplatesMapper
import com.teamforce.thanksapp.domain.models.templates.TemplateModel
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException

class TemplatesPagingSource(
    private val api: ThanksApi,
    private val templatesMapper: TemplatesMapper,
    private val scopeTemplates: ScopeRequestParams,
    private val selectedSections: List<Int>
) : PagingSource<Int, TemplateModel>() {

    override fun getRefreshKey(state: PagingState<Int, TemplateModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TemplateModel> {
        var pageIndex = params.key ?: 1

        if (params is LoadParams.Refresh) {
            pageIndex = 1
        }

        return try {
            val response = api.getTemplates(
                limit = Consts.PAGE_SIZE,
                offset = pageIndex,
                scope = scopeTemplates.id.toString(),
                selectedSections = selectedSections
            )
            val nextKey =
                if (response.isEmpty()) {
                    null
                } else {
                    pageIndex + (params.loadSize / Consts.PAGE_SIZE)
                }
            LoadResult.Page(
                data = templatesMapper.mapTemplates(response, scopeTemplates),
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