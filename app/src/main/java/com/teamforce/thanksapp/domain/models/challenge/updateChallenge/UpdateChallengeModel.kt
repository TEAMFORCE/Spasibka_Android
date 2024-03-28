package com.teamforce.thanksapp.domain.models.challenge.updateChallenge

import com.teamforce.thanksapp.data.api.ImageFileData
import okhttp3.MultipartBody
import java.time.ZonedDateTime

data class UpdateChallengeModel(
    val name: String,
    val description: String,
    val startAt: ZonedDateTime? = null,
    val endAt: ZonedDateTime? = null,
    val startBalance: Int? = null,
    val winnersCount: Int? = null,
    val accountId: Int? = null,
    val challengeWithVoting: Boolean? = null,
    val multipleReports: Boolean? = null,
    val showContenders: Boolean? = null,
    val fileList: List<ImageFileData>? = null,
    val photos: List<MultipartBody.Part>? = null,
)
