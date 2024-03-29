package com.teamforce.thanksapp.domain.models.templates

import android.os.Parcelable
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class TemplateForBundleModel(
    val id: Int,
    val title: String,
    val description: String,
    val photos: List<String>,
    val scopeOfTemplates: ScopeRequestParams,
    val multipleReports: Boolean,
    val showContenders: Boolean,
    val sections: List<CategoryItem> = emptyList(),
    val challengeWithVoting: Boolean = false
) : Parcelable
