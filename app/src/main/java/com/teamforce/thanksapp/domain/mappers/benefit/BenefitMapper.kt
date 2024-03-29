package com.teamforce.thanksapp.domain.mappers.benefit

import android.content.Context
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.benefit.BenefitItemByIdEntity
import com.teamforce.thanksapp.data.entities.benefit.BenefitItemEntity
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.BenefitModel
import com.teamforce.thanksapp.utils.parseDateTimeWithBindToTimeZone
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BenefitMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun map(from: BenefitItemEntity): BenefitModel {
        return BenefitModel(
            id = from.id,
            name = from.name,
            description = from.description,
            images = from.images,
            status = from.status,
            actualTo = parseDateTimeWithBindToTimeZone(from.actualTo, context),
            rest = from.rest,
            sold = from.sold,
            selected = from.selected,
            price = from.price,
            categories = from.categories,
            orderStatus = from.orderStatus,
            likes_amount = from.likes_amount,
            user_liked =  from.user_liked
        )

    }

    fun mapList(from: List<BenefitItemEntity>): List<BenefitModel> {
        return from.map {
            map(it)
        }
    }

    // TODO Убрать использование context для получения ресурсов
    fun mapEntityByIdToModel(from: BenefitItemByIdEntity): BenefitItemByIdModel {
        return BenefitItemByIdModel(
            id = from.id,
            name = from.name,
            description = from.description,
            images = from.images,
            status = from.status,
            actualTo = parseDateTimeWithBindToTimeZone(from.actualTo, context),
            rest = context.getString(R.string.amount, from.rest),
            sold = context.getString(R.string.amount, from.sold),
            selected = from.selected,
            price = from.price,
            categories = from.categories,
            orderStatus = from.orderStatus,
            likes_amount = from.likes_amount,
            commentsAmount = from.comments_amount,
            user_liked =  from.user_liked,
            selectedByUsers = from.selectedByUsers,
            canReview = from.canReview,
            avgRate = from.avgRate,
            inFavorites = from.inFavorites,
            reviewsAmount = from.reviewsAmount,
            review = mapReviewInBenefit(from.review)
        )
    }

    private fun mapReviewInBenefit(from: BenefitItemByIdEntity.Review?): BenefitItemByIdModel.Review?{
        return from?.let {
            BenefitItemByIdModel.Review(
                id = from.id,
                text = from.text,
                rate = from.rate,
                createdAt = from.createdAt,
                photos = from.photos?.mapNotNull { it } ?: listOf()
            )
        }
    }
}