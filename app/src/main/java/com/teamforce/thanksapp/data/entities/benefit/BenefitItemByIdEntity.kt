package com.teamforce.thanksapp.data.entities.benefit

import com.google.gson.annotations.SerializedName

data class BenefitItemByIdEntity(
    val id: Int,
    val name: String,
    val images: List<BenefitItemEntity.Image>?,
    val status: String,
    @SerializedName("selected_by_users")
    val selectedByUsers: Int,
    @SerializedName("order_status")
    val orderStatus: OrderStatus,
    val description: String,
    @SerializedName("actual_to")
    val actualTo: String?,
    val rest: Int,
    val sold: Int,
    val likes_amount: Int,
    val comments_amount: Int,
    val user_liked: Boolean,
    val selected: Int,
    val price: BenefitItemEntity.Price?,
    val categories: List<BenefitItemEntity.CategoryInBenefitItem>,
    @SerializedName("can_review")
    val canReview: Boolean,
    @SerializedName("in_favorites")
    val inFavorites: Boolean,
    @SerializedName("reviews_amount")
    val reviewsAmount: Int,
    @SerializedName("avg_rate")
    val avgRate: Float,
    val review: Review?
){
    data class Review(
        val id: Long,
        val text: String,
        val rate: Int,
        @SerializedName("created_at")
        val createdAt: String,
        val photos: List<String?>?
    )
    enum class OrderStatus(val nameOrderStatus: String){
        C("C"), O("O"), N("N")
    }
}
