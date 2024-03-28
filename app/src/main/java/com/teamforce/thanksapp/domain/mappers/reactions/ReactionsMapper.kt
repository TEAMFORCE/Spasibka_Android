package com.teamforce.thanksapp.domain.mappers.reactions

import com.teamforce.thanksapp.data.response.LikeResponse
import com.teamforce.thanksapp.domain.models.reactions.LikeResponseModel
import javax.inject.Inject

class ReactionsMapper @Inject constructor(

) {
    fun mapLikeResponse(from: LikeResponse, position: Int?): LikeResponseModel {
        return LikeResponseModel(
            position = position,
            isLiked = from.isLiked,
            userId = from.user_id,
            likesAmount = from.likesAmount
        )
    }
}