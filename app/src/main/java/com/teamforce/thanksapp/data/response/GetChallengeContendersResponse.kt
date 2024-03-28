package com.teamforce.thanksapp.data.response

import com.google.gson.annotations.SerializedName

data class GetChallengeContendersResponse(
    val data: List<Contender>?
){
    data class Contender(
        val participant_id: Int,
        val participant_photo: String?,
        val participant_name: String,
        var comments_amount: Int = 0,
        @SerializedName("user_liked")
        var userLiked: Boolean,
        var likes_amount: Int = 0,
        var open: Boolean = false,
        val can_approve: Boolean,
        val my_report: Boolean,
        val participant_surname: String,
        val report_created_at: String,
        val report_text: String,
        val report_photo: String?,
        val report_id: Int
    )
}
