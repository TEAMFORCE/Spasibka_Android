package com.teamforce.thanksapp.data.entities.benefit

import com.google.gson.annotations.SerializedName

data class BenefitItemEntity(
    val id: Int,
    val name: String,
    val images: List<Image>?,
    val status: String,
    @SerializedName("order_status")
    val orderStatus: BenefitItemByIdEntity.OrderStatus,
    val description: String,
    @SerializedName("actual_to")
    val actualTo: String?,
    val rest: Int,
    val sold: Int,
    val selected: Int,
    val likes_amount: Int,
    val user_liked: Boolean,
    val price: Price?,
    val categories: List<CategoryInBenefitItem>
){
    data class Price(
        @SerializedName("price_in_thanks")
        val priceInThanks: Int,
        @SerializedName("price_not_in_thanks")
        val priceNotInThanks: Int
    )

    data class Image(
        val id: Int,
        val link: String?,
        @SerializedName("for_showcase")
        val forShowcase: Boolean
    )

    data class CategoryInBenefitItem(
        @SerializedName("category_id")
        val categoryId: Int,
        @SerializedName("category_name")
        val categoryName: String,
    )
}
