package com.teamforce.thanksapp.data.entities.templates

import com.teamforce.thanksapp.data.entities.challenges.SectionsData

data class TemplateEntity(
    val id: Int,
    val challenge_type: String,
    val description: String,
    val name: String,
    val multiple_reports: Boolean,
    val show_contenders: Boolean,
    val parameters: Any?,
    val photos: List<String?>?,
    val sections: List<SectionsData> = emptyList(),
    val status: List<String>?,
)