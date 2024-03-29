package com.teamforce.thanksapp.data.entities.events

import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.model.domain.TagModel

data class EventEntity(
    val filter: EventFilterEntity,
    val data: List<EventDataEntity>
)
data class EventFilterEntity(
    val eventtypes: List<EventTypeEntity>
)
data class EventTypeEntity(
    val id: Int,
    val name: String,
    val on: Boolean
)

data class EventDataEntity(
    val id: Int,
    val time: String,
    val header: String?,
    val icon: String?,
    val text: String?,
    val selector: String,
    val mainlink: String?,
    val recipient: String?,
    @SerializedName("text_icon")
    val textIcon: String?,
    @SerializedName("end_at")
    val endAt: String?,
    @SerializedName("user_liked")
    val userLiked: Boolean,
    val photos: List<String?>?,
    @SerializedName("likes_amount")
    val likesAmount: Int,
    @SerializedName("comments_amount")
    val commentsAmount: Int,
    val tags: List<TagEventEntity>?
)

data class TagEventEntity(
    val id: Int,
    val name: String
)
