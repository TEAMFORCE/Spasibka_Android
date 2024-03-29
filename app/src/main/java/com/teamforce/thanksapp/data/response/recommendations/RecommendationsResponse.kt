package com.teamforce.thanksapp.data.response.recommendations

import com.google.gson.annotations.SerializedName

data class RecommendationsResponse(
    val status: Int,
    val data: List<RecommendNetworkEntity>
){
    data class RecommendNetworkEntity(
        val id: Long,
        val name: String,
        val header: String,
        val type: String,
        val photos: List<String>?,
        @SerializedName("is_new")
        val isNew: Boolean?,
        @SerializedName("marketplace_id")
        val marketplaceId: Int?
    )
}
