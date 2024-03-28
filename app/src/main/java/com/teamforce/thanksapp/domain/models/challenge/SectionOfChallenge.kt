package com.teamforce.thanksapp.domain.models.challenge

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class SectionOfChallenge(
    val title: String,
    val type: SectionsOfChallengeType
)

@Parcelize
enum class SectionsOfChallengeType : Parcelable {
    DETAIL, COMMENTS, WINNERS, CONTENDERS, MY_RESULT, REACTIONS, DEPENDENCIES
}