package com.teamforce.thanksapp.presentation.fragment.challenges.category

import com.teamforce.thanksapp.data.entities.challenges.GetSectionsEntity
import com.teamforce.thanksapp.data.entities.challenges.SectionsData
import javax.inject.Inject

class CategoryMapper @Inject constructor() {
    private fun List<CategoryItem>.resolveParent(parentId: Int? = null): List<CategoryItem> {
        return map {
            it.copy(parentId = parentId, categories = it.categories.resolveParent(it.id))
        }
    }

    private fun GetSectionsEntity.toModels(): List<CategoryItem> {
        return mapItems(data).resolveParent()
    }

    fun mapItems(data: List<SectionsData>): List<CategoryItem> {
        return data.map {
            CategoryItem(
                id = it.id,
                name = it.name,
                categories = it.children?.let { nonNullChildren -> mapItems(nonNullChildren) } ?: emptyList()
            )
        }
    }

    fun toCategoryItems(sectionsEntity: GetSectionsEntity): List<CategoryItem> =
        sectionsEntity.toModels()

    private fun getMockItems(): List<CategoryItem> = listOf(
        CategoryItem(
            id = 1,
            name = "Спорт",
            categories = listOf(
                CategoryItem(id = 2, name = "Воллейбол"),
                CategoryItem(id = 3, name = "Большой теннис"),
                CategoryItem(
                    id = 11,
                    name = "Теннис",
                    categories = listOf(
                        CategoryItem(id = 4, name = "Большой теннис"),
                        CategoryItem(id = 5, name = "Настольный теннис"),
                        CategoryItem(id = 6, name = "Мини-теннис"),
                        CategoryItem(id = 7, name = "Какой-то баскетбол"),
                        CategoryItem(id = 8, name = "И еще какой-то баскетбол"),
                    )
                )
            )
        ),
        CategoryItem(id = 9, name = "Автомобили"),
        CategoryItem(id = 10, name = "Офис"),
    )
}