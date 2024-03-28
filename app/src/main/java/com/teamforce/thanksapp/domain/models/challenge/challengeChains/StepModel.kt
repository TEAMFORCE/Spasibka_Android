package com.teamforce.thanksapp.domain.models.challenge.challengeChains

import com.teamforce.thanksapp.domain.models.challenge.ChallengeCondition
import com.teamforce.thanksapp.presentation.fragment.challenges.category.DisplayableItem

data class StepModel(
    val step: Int = 0,
    val status: StepStatus = StepStatus.BLOCKED,
    val info_msg: String,
    var isOpen: Boolean = false,
    val tasks: List<Task>
){
    enum class StepStatus{
        CURRENT, COMPLETED, BLOCKED
    }

    data class Task(
        val id: Long,
        val name: String,
        val creatorName: String,
        val reward: Int,
        val image: String?,
        val status: ChallengeCondition
    ): DisplayableItem
}
