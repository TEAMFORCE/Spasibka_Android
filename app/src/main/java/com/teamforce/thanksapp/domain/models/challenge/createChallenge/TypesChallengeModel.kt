package com.teamforce.thanksapp.domain.models.challenge.createChallenge

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TypesChallengeModel(
    val default: Int,
    val voting: Int
) : Parcelable
