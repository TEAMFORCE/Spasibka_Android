package com.teamforce.thanksapp.data.entities.challenges.challengeChains

import com.google.gson.annotations.SerializedName

data class ParticipantChainEntity(
    @SerializedName("participant_id")
    val participantId: Long,
    @SerializedName("participant_name")
    val participantName: String,
    @SerializedName("participant_photo")
    val participantPhoto: String?
)
