package com.teamforce.thanksapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.teamforce.thanksapp.data.api.ImageFileData
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.sources.templates.TemplatesPagingSource
import com.teamforce.thanksapp.domain.mappers.templates.TemplatesMapper
import com.teamforce.thanksapp.domain.models.templates.TemplateModel
import com.teamforce.thanksapp.domain.repositories.TemplatesRepository
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject

class TemplatesRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val templatesMapper: TemplatesMapper
): TemplatesRepository {

    override fun getTemplates(scopeTemplates: ScopeRequestParams, selectedSections: List<Int> ): Flow<PagingData<TemplateModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                TemplatesPagingSource(
                    api = thanksApi,
                    templatesMapper = templatesMapper,
                    scopeTemplates = scopeTemplates,
                    selectedSections = selectedSections
                )
            }
        ).flow
    }

    override suspend fun createTemplate(
        scopeTemplates: String,
        name: String,
        description: String,
        photos: List<MultipartBody.Part?>,
        challengeType: String,
        severalReports: String,
        showContenders: String,
        sections: List<Int>
    ): ResultWrapper<Any> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.createTemplate(
                scope = scopeTemplates,
                name = name,
                description = description,
                photos = photos,
                challengeType = challengeType,
                severalReports = severalReports,
                showContenders = showContenders,
                sections = sections.joinToString(",")
            )
        }
    }

    override suspend fun updateTemplate(
        templateId: Int,
        scopeTemplates: String,
        name: String,
        description: String,
        photos: List<MultipartBody.Part>,
        fileList: List<ImageFileData>?,
        challengeType: String,
        severalReports: String,
        showContenders: String,
        sections: List<Int>
    ): ResultWrapper<Any> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.updateTemplate(
                templateId = templateId,
                scope = scopeTemplates,
                name =  name,
                description = description,
                photos = photos,
                // TODO Почему нам нужно еще маппить тут fileList
                fileList = fileList?.let { mapOf("fileList" to it) },
                sections = sections.joinToString(","),
                challengeType = challengeType,
                severalReports = severalReports,
                showContenders = showContenders,
            )
        }
    }

}