package com.teamforce.thanksapp.domain.models.benefit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class RatingModel(
    val review: Review?,
    val avgRate: Float,
    val canReview: Boolean
){
    @Parcelize
    data class Review(
        val id: Long,
        val text: String,
        val rate: Int,
        val createdAt: String,
        val photos: List<String>
    ) : Parcelable
}
