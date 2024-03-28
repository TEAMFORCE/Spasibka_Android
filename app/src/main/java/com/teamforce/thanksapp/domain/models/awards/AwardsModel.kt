package com.teamforce.thanksapp.domain.models.awards

import android.os.Parcelable
import com.teamforce.thanksapp.presentation.fragment.challenges.category.DisplayableItem
import kotlinx.parcelize.Parcelize

data class CategoryAwardsModel(
    val id: Long,
    val name: String,
    val awards: List<AwardsModel>
)
@Parcelize
data class AwardsModel(
    val id: Long,
    val name: String? = "",
    val cover: String?,
    val targetScores: Int,
    val reward: Int,
    val description: String,
    val currentScores: Int,
    val state: AwardState,
    val categoryId: Long?,
    val categoryName: String = "",
    val obtainedAt: String?
): DisplayableItem, Parcelable

enum class AwardState{
    RECEIVED, CAN_BE_RECEIVED, NOT_AVAILABLE, SET_IN_STATUS
}
