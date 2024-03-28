package com.teamforce.thanksapp.domain.mappers.awards

import android.content.Context
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.response.awards.AwardEntity
import com.teamforce.thanksapp.domain.models.awards.AwardState
import com.teamforce.thanksapp.domain.models.awards.AwardsModel
import com.teamforce.thanksapp.domain.models.awards.CategoryAwardsModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.parseDateTimeOutputOnlyDate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AwardsMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun mapAwardsListToCategoryList(from: List<AwardEntity>): List<CategoryAwardsModel> {
        val groupedAwards = from.groupBy { it.categoryId }

        return groupedAwards.values.map { categories ->
            CategoryAwardsModel(
                id = categories.first().categoryId ?: Consts.OTHER_CATEGORY_ID,
                name = categories.first().categoryName ?: context.getString(R.string.awards_other),
                awards = categories.map(::mapAwardEntityToModel)
            )
        }
    }

    private fun mapAwardEntityToModel(from: AwardEntity): AwardsModel {
        return AwardsModel(
            id = from.id,
            name = from.name,
            categoryId = from.categoryId,
            categoryName = from.categoryName ?: "",
            cover = from.cover,
            targetScores = from.targetScores ?: 0,
            currentScores = from.currentScores ?: 0,
            state = determineState(from),
            description = from.description ?: "",
            reward = from.reward ?: 0,
            obtainedAt = from.obtainedAt
        )
    }

    private fun determineState(
        from: AwardEntity
    ): AwardState {
        return if (from.received) AwardState.RECEIVED
        else if (from.targetScores == 0) AwardState.NOT_AVAILABLE
        else if ((from.currentScores ?: 0) >= (from.targetScores ?: 0)) AwardState.CAN_BE_RECEIVED
        else if (from.inStatus == true) AwardState.SET_IN_STATUS
        else AwardState.NOT_AVAILABLE
    }

}