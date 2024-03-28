package com.teamforce.thanksapp.domain.models.events

import com.teamforce.thanksapp.domain.models.general.ObjectsToLike


data class EventDataModel(
    val id: Int,
    val time: String,
    val header: String?,
    val icon: String?,
    val text: String,
    val mainlink: String?,
    val recipient: String?,
    val endAt: String?,
    val textIcon: String?,
    val typeOfObject: ObjectsToLike?,
    var userLiked: Boolean,
    val photos: List<String?>?,
    var likesAmount: Int,
    val commentsAmount: Int,
    val tags: List<TagEventModel>?,
    val canBeReacted: Boolean
)

data class TagEventModel(
    val id: Int,
    val name: String
)
