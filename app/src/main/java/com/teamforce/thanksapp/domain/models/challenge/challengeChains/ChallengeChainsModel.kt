package com.teamforce.thanksapp.domain.models.challenge.challengeChains

import com.teamforce.thanksapp.presentation.fragment.challenges.category.DisplayableItem


data class ChallengeChainsModel(
    val id: Long,
    val name: String,
    val author: String,
    val authorId: Int,
    val authorPhoto: String?,
    val photos: List<String>,
    val updatedAt: String,
    val contendersTotal: Int,
    val commentsTotal: Int,
    val taskTotal: Int,
    val tasksFinished: Int,
    val currentState: ChainCondition
): DisplayableItem
