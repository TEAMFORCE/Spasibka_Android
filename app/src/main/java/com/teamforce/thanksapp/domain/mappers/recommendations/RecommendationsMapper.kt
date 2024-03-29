package com.teamforce.thanksapp.domain.mappers.recommendations

import com.teamforce.thanksapp.data.response.recommendations.RecommendationsResponse
import com.teamforce.thanksapp.domain.models.recommendations.RecommendModel
import javax.inject.Inject

class RecommendationsMapper @Inject constructor(

) {
    fun mapRecommendsList(from: List<RecommendationsResponse.RecommendNetworkEntity>): List<RecommendModel>{
        return from.mapNotNull ( ::mapRecommendNetworkEntityToModel )
    }

    private fun mapRecommendNetworkEntityToModel(from: RecommendationsResponse.RecommendNetworkEntity): RecommendModel? {
        val type = RecommendModel.RecommendObjectType.safetyValueOf(from.type.uppercase())
        return if (type != null) {
            RecommendModel(
                id = from.id,
                name = from.name,
                header = from.header,
                type = type,
                photo = from.photos?.firstOrNull(),
                isNew = from.isNew ?: false,
                marketplaceId = from.marketplaceId
            )
        } else {
            null
        }
    }
}