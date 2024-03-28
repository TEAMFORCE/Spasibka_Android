package com.teamforce.thanksapp.data.entities.benefit

import com.google.gson.annotations.SerializedName

data class ReviewNetworkEntity(
    val id: Long,
    @SerializedName("author_id")
    val authorId: Long,
    @SerializedName("author_name")
    val authorName: String,
    @SerializedName("author_photo")
    val authorPhoto: String?,
    val text: String?,
    val rate: Int,
    @SerializedName("likes_amount")
    val likesAmount: Int,
    @SerializedName("user_liked")
    val isLiked: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("is_my_review")
    val isMyReview: Boolean,
    val photos: List<String?>?
)
