package com.teamforce.thanksapp.data.entities.challenges.challengeChains

import com.google.gson.annotations.SerializedName

data class ChainEntity(
    val id: Long,
    val name: String,
    val description: String,
    val author: String,
    @SerializedName("author_id")
    val authorId: Int,
    @SerializedName("author_photo")
    val authorPhoto: String?,
    val photos: List<String?>?,
    @SerializedName("start_at")
    val startAt: String?,
    @SerializedName("end_at")
    val endAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("contenders_total")
    val contendersTotal: Int,
    @SerializedName("comments_total")
    val commentsTotal: Int,
    @SerializedName("task_total")
    val taskTotal: Int,
    @SerializedName("tasks_finished")
    val tasksFinished: Int,
    @SerializedName("current_state")
    val currentState: String
)
