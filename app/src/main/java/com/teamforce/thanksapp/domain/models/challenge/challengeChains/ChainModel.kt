package com.teamforce.thanksapp.domain.models.challenge.challengeChains

data class ChainModel(
    val id: Long,
    val name: String,
    val description: String,
    val author: String,
    val authorId: Int,
    val authorPhoto: String?,
    val photos: List<String>,
    val updatedAt: String?,
    val startAt: String?,
    val endAt: String?,
    val contendersTotal: Int,
    val commentsTotal: Int,
    val taskTotal: Int,
    val tasksFinished: Int,
    val currentState: ChainCondition
)
