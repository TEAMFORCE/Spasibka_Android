package com.teamforce.thanksapp.data.response.awards

import com.google.gson.annotations.SerializedName

data class GetAwardsResponse(
    val status: Int,
    val data: List<AwardEntity>
)

data class AwardEntity(
    val id: Long,
    val name: String?,
    val cover: String?,
    val description: String?,
    val reward: Int?,
    @SerializedName("to_score")
    val targetScores: Int?,
    @SerializedName("scored")
    val currentScores: Int?,
    val received: Boolean,
    @SerializedName("category_id")
    val categoryId: Long?,
    @SerializedName("category_name")
    val categoryName: String?,
    @SerializedName("for_profile")
    val inStatus: Boolean?,
    @SerializedName("obtained_at")
    val obtainedAt: String?
)
