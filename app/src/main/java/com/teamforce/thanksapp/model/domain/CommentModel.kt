package com.teamforce.thanksapp.model.domain

import com.google.gson.annotations.SerializedName

data class CommentModel(
    val id: Int,
    val text: String,
    val picture: String?,
    val gif: String?,
    val created: String,
    val edited: String,
    @SerializedName("user_liked")
    var userLiked: Boolean,
    @SerializedName("likes_amount")
    var likesAmount: Int,
    val user: User,

){
    data class User(
        val id: Int,
        val name: String,
        val surname: String,
        val avatar: String?
    )
}
