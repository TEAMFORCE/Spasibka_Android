package com.teamforce.thanksapp.domain.models.general

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ObjectsToLike(val idForLike: String, val eventSelector: String) : Parcelable {
    CHALLENGE( "challenge_id", "Q"),
    REPORT_OF_CHALLENGE("challenge_report_id", "R"),
    TRANSACTION("transaction", "T"),
    OFFER("offer_id", ""),
    PURCHASE("order_id", "P"),
    BIRTHDAY("B", "B"),
    COMMENT("comment_id", "Comment"),
    OFFER_REVIEW("offer_review_id", "Offer_review")
}