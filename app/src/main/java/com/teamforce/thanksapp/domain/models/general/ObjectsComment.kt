package com.teamforce.thanksapp.domain.models.general

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ObjectsComment: Parcelable {
    CHALLENGE,
    REPORT_OF_CHALLENGE,
    TRANSACTION,
    OFFER
}

