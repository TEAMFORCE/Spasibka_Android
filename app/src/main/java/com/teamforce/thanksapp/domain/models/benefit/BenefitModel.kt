package com.teamforce.thanksapp.domain.models.benefit

import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.data.entities.benefit.BenefitItemByIdEntity
import com.teamforce.thanksapp.data.entities.benefit.BenefitItemEntity

data class BenefitModel(
    val id: Int,
    val name: String,
    val images: List<BenefitItemEntity.Image>?,
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
    val price: BenefitItemEntity.Price?,
    val categories: List<BenefitItemEntity.CategoryInBenefitItem>
)

