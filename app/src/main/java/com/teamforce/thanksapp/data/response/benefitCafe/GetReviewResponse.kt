package com.teamforce.thanksapp.data.response.benefitCafe

import com.teamforce.thanksapp.data.entities.benefit.ReviewNetworkEntity

data class GetReviewResponse(
    val status: Int,
    val data: List<ReviewNetworkEntity>
)
