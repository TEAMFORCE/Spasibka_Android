package com.teamforce.thanksapp.domain.models.challenge

enum class ChallengeCondition (val condition: String) {
    ACTIVE("A"), FINISHED("F"), DEFERRED("W")
}