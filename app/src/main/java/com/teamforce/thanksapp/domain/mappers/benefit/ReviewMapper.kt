package com.teamforce.thanksapp.domain.mappers.benefit

import com.teamforce.thanksapp.data.entities.benefit.ReviewNetworkEntity
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.RatingModel
import com.teamforce.thanksapp.domain.models.benefit.ReviewModel
import javax.inject.Inject

class ReviewMapper @Inject constructor() {

    fun map(from: ReviewNetworkEntity): ReviewModel {
        return ReviewModel(
            id = from.id,
            authorId = from.authorId,
            authorName = from.authorName,
            authorPhoto = from.authorPhoto,
            text = from.text,
            isMyReview = from.isMyReview,
            isLiked = from.isLiked,
            likesAmount = from.likesAmount,
            createdAt = from.createdAt,
            photos = from.photos ?: listOf(),
            rate = from.rate
        )

    }

    fun mapReviewsList(from: List<ReviewNetworkEntity>): List<ReviewModel> {
        return from.map {
            map(it)
        }
    }

    fun mapBenefitModelToRatingModel(from: BenefitItemByIdModel): RatingModel{
        return RatingModel(
            review = mapReviewModelToRatingModelReview(from.review),
            avgRate = from.avgRate,
            canReview = from.canReview
        )
    }

    private fun mapReviewModelToRatingModelReview(from: BenefitItemByIdModel.Review?): RatingModel.Review?{
        return from?.let {
            RatingModel.Review(
                id = from.id,
                text = from.text,
                rate = from.rate,
                createdAt = from.createdAt,
                photos = from.photos
            )
        }
    }
}