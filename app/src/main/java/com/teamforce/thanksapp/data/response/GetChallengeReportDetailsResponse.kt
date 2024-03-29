package com.teamforce.thanksapp.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetChallengeReportDetailsResponse(
    val challenge: Challenge,
    val user: User,
    @SerializedName("text")
    val challengeText: String,
    @SerializedName("photo")
    val challengePhoto: String?,
    val my_report: Boolean,
    val created_at: String,
    val likes_amount: Int = 0,
    val user_liked: Boolean,
    val comments_amount: Int = 0,
    val points: Int?,

    ) : Parcelable {
    @Parcelize
    data class Challenge(
        val id: Int,
        val name: String
    ) : Parcelable

    @Parcelize
    data class User(
        val id: Int,
        val tg_name: String,
        val name: String,
        val surname: String,
        val avatar: String?
    ) : Parcelable
}
