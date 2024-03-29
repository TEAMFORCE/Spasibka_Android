package com.teamforce.thanksapp.domain.models.challenge.createChallenge

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DebitAccountModel(
    val accountType: String,
    val amount: Int,
    val id: Int,
    val organizationName: String,
    val ownerId: Int,
    val myAccount: Boolean,
    var current: Boolean = false
) : Parcelable
