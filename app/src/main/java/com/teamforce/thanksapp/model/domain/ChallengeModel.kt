package com.teamforce.thanksapp.model.domain

import android.os.Parcelable
import com.teamforce.thanksapp.domain.models.challenge.ChallengeCondition
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChallengeModel(
    val id: Int,
    val name: String?,
    val photo: List<String>?,
    val updatedAt: String?,
    val states: List<String?>?,
    val startBalance: Int?,
    val creatorId: Int,
    val creatorName: String,
    val creatorSurname: String,
    val winnersCount: Int,
    val parameters: List<ParameterModel>?,
    val challengeCondition: ChallengeCondition,
    val approvedReportsAmount: Int?,
    val fund: Int,
    val status: String?,
    val isNewReports: Boolean?,
    val prizeSize: Int?,
    val awardees: Int?,
    val active: Boolean,
    val fromOrganization: Boolean,
    val organizationName: String?
): Parcelable
