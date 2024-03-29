package com.teamforce.thanksapp.domain.models.reactions


data class LikeResponseModel(
    val position: Int?,
    val isLiked: Boolean,
    val userId: Int,
    val likesAmount: Int
)
