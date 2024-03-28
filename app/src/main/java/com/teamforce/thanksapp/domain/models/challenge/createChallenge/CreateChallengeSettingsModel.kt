package com.teamforce.thanksapp.domain.models.challenge.createChallenge

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreateChallengeSettingsModel(
    val accounts: List<DebitAccountModel>,
    val multipleReports: Boolean,
    val showContenders: Boolean,
    val challengeWithVoting: Boolean
) : Parcelable
