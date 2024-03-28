package com.teamforce.thanksapp.domain.models.challenge


data class ContenderModel(
    val participantId: Int,
    val participantPhoto: String?,
    val participantName: String,
    var commentsAmount: Int = 0,
    var userLiked: Boolean,
    var likesAmount: Int = 0,
    var open: Boolean = false,
    val canApprove: Boolean,
    val myReport: Boolean,
    val participantSurname: String,
    val reportCreatedAt: String,
    val reportText: String,
    val reportPhoto: String?,
    val reportId: Int
)
