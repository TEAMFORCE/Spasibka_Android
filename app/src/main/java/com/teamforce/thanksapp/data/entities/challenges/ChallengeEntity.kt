package com.teamforce.thanksapp.data.entities.challenges

import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.model.domain.ParameterModel

data class ChallengeEntity(
    val id: Int,
    val name: String?,
    val photo: String?,
    val photos: List<String?>,
    @SerializedName("updated_at")
    val updatedAt: String?,
    val states: List<String?>?,
    @SerializedName("start_balance")
    val startBalance: Int?,
    @SerializedName("creator_id")
    val creatorId: Int,
    @SerializedName("creator_name")
    val creatorName: String,
    @SerializedName("creator_surname")
    val creatorSurname: String,
    @SerializedName("winners_count")
    val winnersCount: Int,
    val parameters: List<ParameterModel>?,
    @SerializedName("challenge_condition")
    val challengeCondition: String,
    @SerializedName("approved_reports_amount")
    val approvedReportsAmount: Int?,
    val fund: Int,
    val status: String?,
    @SerializedName("is_new_reports")
    val isNewReports: Boolean?,
    @SerializedName("prize_size")
    val prizeSize: Int = 0,
    val awardees: Int?,
    val active: Boolean,
    @SerializedName("from_organization")
    val fromOrganization: Boolean,
    @SerializedName("organization_name")
    val organizationName: String?,
    val step: Int = 0,
)
