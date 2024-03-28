package com.teamforce.thanksapp.domain.repositories

import androidx.paging.PagingData
import com.teamforce.thanksapp.data.api.ImageFileData
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.domain.models.templates.TemplateModel
import com.teamforce.thanksapp.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface TemplatesRepository {

    fun getTemplates(scopeTemplates: ScopeRequestParams, selectedSections: List<Int> = emptyList()): Flow<PagingData<TemplateModel>>

    suspend fun createTemplate(
        scopeTemplates: String,
        name: String,
        description: String,
        photos: List<MultipartBody.Part?>,
        challengeType: String,
        severalReports: String,
        showContenders: String,
        sections: List<Int>,
    ): ResultWrapper<Any>

    suspend fun updateTemplate(
        templateId: Int,
        scopeTemplates: String,
        name: String,
        description: String,
        photos: List<MultipartBody.Part>,
        fileList: List<ImageFileData>? = null,
        challengeType: String,
        severalReports: String,
        showContenders: String,
        sections: List<Int>,
        ): ResultWrapper<Any>
}