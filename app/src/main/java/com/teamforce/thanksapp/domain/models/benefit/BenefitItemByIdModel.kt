package com.teamforce.thanksapp.domain.models.benefit

import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.data.entities.benefit.BenefitItemByIdEntity
import com.teamforce.thanksapp.data.entities.benefit.BenefitItemEntity

data class BenefitItemByIdModel(
    val id: Int,
    val name: String,
    val images: List<BenefitItemEntity.Image>?,
    val status: String,
    @SerializedName("selected_by_users")
    val selectedByUsers: Int,
    @SerializedName("order_status")
    val orderStatus: BenefitItemByIdEntity.OrderStatus,
    val description: String,
    @SerializedName("actual_to")
    val actualTo: String?,
    val rest: String,
    val sold: String,
    val selected: Int,
    val likes_amount: Int,
    val commentsAmount: Int,
    val user_liked: Boolean,
    val price: BenefitItemEntity.Price?,
    val categories: List<BenefitItemEntity.CategoryInBenefitItem>,
    val canReview: Boolean,
    val inFavorites: Boolean,
    val reviewsAmount: Int,
    val avgRate: Float,
    val review: Review?
){
    data class Review(
        val id: Long,
        val text: String,
        val rate: Int,
        val createdAt: String,
        val photos: List<String>
    )
}

