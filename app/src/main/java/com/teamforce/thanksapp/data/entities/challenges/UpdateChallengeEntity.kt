package com.teamforce.thanksapp.data.entities.challenges

import com.google.gson.annotations.SerializedName

data class UpdateChallengeEntity(
    val name: String,
    val description: String,
    @SerializedName("start_at")
    val startAt: String?,
    @SerializedName("end_at")
    val endAt: String?,
    @SerializedName("start_balance")
    val startBalance: Int?,
    @SerializedName("winners_count")
    val winnersCount: Int?,
    @SerializedName("account_id")
    val accountId: Int?,
    @SerializedName("challenge_type")
    val challengeType: String?,
    @SerializedName("multiple_reports")
    val multipleReports: Boolean?,
    @SerializedName("show_contenders")
    val showContenders: Boolean?,
)
