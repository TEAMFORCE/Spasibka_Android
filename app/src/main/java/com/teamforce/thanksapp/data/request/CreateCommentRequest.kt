package com.teamforce.thanksapp.data.request

import okhttp3.MultipartBody

data class CreateCommentRequest(
    var challenge_id: Int? = null,
    var transaction_id: Int? = null,
    var challenge_report_id: Int? = null,
    var offer_id: Int? = null,
    val text: String,
    val gif: String? = null,
    val picture: MultipartBody.Part?
)
