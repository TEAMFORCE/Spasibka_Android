package com.teamforce.thanksapp.data.entities.challenges

data class GetSectionsEntity(
    val data: List<SectionsData>,
)

data class SectionsData(
    val children: List<SectionsData>?,
    val id: Int,
    val name: String,
)
