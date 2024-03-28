package com.teamforce.thanksapp.data.response

import com.google.gson.annotations.SerializedName

data class GetReactionsResponse(
    @SerializedName("transaction_id")
    val transactionId: Int,
    @SerializedName("challenge_id")
    val challengeId: Int,
    @SerializedName("challenge_report_id")
    val reportOfChallengeId: Int,
    @SerializedName("likes")
    val likes: List<Like>,
){
    data class Like(
        val like_kind: LikeKind,
        val items: List<InnerInfoLike>
    )

    data class LikeKind(
        val id: Int,
        val code: String,
        val name: String,
        val icon: String?
    )

    data class InnerInfoLike(
        val time_of: String,
        val user: UserInLike
    )
    data class UserInLike(
        val id: Int,
        val name: String?,
        val avatar: String?
    )
}
