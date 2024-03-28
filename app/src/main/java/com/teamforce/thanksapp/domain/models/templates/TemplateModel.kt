package com.teamforce.thanksapp.domain.models.templates

import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryItem

data class TemplateModel(
    val challengeType: Boolean,
    val description: String,
    val id: Int,
    val name: String,
    val multipleReports: Boolean,
    val showContenders: Boolean,
    val parameters: Any?,
    val photos: List<String>,
    val sections: List<CategoryItem> = emptyList(),
    val status: List<String>?,
    val editableByMe: Boolean = false,
    val scope: ScopeRequestParams,
)
