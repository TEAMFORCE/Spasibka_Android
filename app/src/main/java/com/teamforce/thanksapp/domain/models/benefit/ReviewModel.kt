package com.teamforce.thanksapp.domain.models.benefit

import com.google.gson.annotations.SerializedName

data class ReviewModel(
    val id: Long,
    val authorId: Long,
    val authorName: String,
    val authorPhoto: String?,
    val text: String?,
    val rate: Int,
    var likesAmount: Int,
    var isLiked: Boolean,
    val createdAt: String,
    val isMyReview: Boolean,
    val photos: List<String?>
)
