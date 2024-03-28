package com.teamforce.thanksapp.data.entities.challenges.challengeChains

import com.google.gson.annotations.SerializedName

data class ChallengeChainsEntity(
    val id: Long,
    val name: String,
    val author: String,
    @SerializedName("author_id")
    val authorId: Int,
    @SerializedName("author_photo")
    val authorPhoto: String?,
    val photos: List<String?>?,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("contenders_total")
    val contendersTotal: Int,
    @SerializedName("comments_total")
    val commentsTotal: Int,
    @SerializedName("tasks_total")
    val taskTotal: Int,
    @SerializedName("tasks_finished")
    val tasksFinished: Int,
    @SerializedName("current_state")
    val currentState: String
)

