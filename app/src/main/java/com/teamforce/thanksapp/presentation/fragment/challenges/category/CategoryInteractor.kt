package com.teamforce.thanksapp.presentation.fragment.challenges.category

import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.mapWrapperData
import javax.inject.Inject

class CategoryInteractor @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val categoryMapper: CategoryMapper,
) {

    private fun List<CategoryItem>.findById(id: Int): CategoryItem? {
        val uuu = this
        val items = firstOrNull { it.id == id }

        if (items != null) {
            return items
        } else {

            for (gg in uuu) {
                val hh = gg.categories.findById(id)

                if (hh != null) return hh
            }
        }
        return null
    }

    suspend fun getSections(scope: ScopeRequestParams): ResultWrapper<List<CategoryItem>> =
        challengeRepository.getSections(scope).mapWrapperData { categoryMapper.toCategoryItems(it) }

    suspend fun removeSections(sectionId: Int) = challengeRepository.removeSections(sectionId)
    suspend fun updateSections(sectionId: Int, sectionName: String, parentSectionsIds: List<Int>) =
        challengeRepository.updateSections(
            sectionId = sectionId,
            sectionName = sectionName,
            parentSectionsIds = parentSectionsIds,
        )

    suspend fun createSections(sectionName: String, parentSectionsIds: List<Int>, section: ScopeRequestParams) =
        challengeRepository.createSections(
            sectionName = sectionName,
            section = section,
            parentSectionsIds = parentSectionsIds,
        )
}

fun List<CategoryItem>.updateCheckedState(
    id: Int,
    isChecked: Boolean,
    forceChange: Boolean = false,
): List<CategoryItem> {
    return map {
        if (id == it.id || forceChange) {
            it.copy(
                isChecked = isChecked,
                categories = it.categories.updateCheckedState(id, isChecked, true)
            )
        } else {
            it.copy(
                categories = it.categories.updateCheckedState(id, isChecked, false)
            )
        }
    }
}

fun List<CategoryItem>.findParent(id: Int): CategoryItem? {
    val itemList = this
    val item = firstOrNull { categoryItem -> categoryItem.categories.any { item -> item.id == id } }

    return item ?: run {
        for (item1 in itemList) {
            val categoryItem = item1.categories.findParent(id)
            categoryItem?.let { return it }
        }
        return null
    }
}

fun List<CategoryItem>.findLayerById(id: Int): List<CategoryItem>? {
    val itemList = this
    val item = firstOrNull { it.id == id }

    if (item != null) {
        return itemList
    } else {
        for (item1 in itemList) {
            val hh = item1.categories.findLayerById(id)
            hh?.let {
                return it
            }
        }
    }
    return null
}
