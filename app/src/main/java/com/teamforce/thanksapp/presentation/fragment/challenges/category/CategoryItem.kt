package com.teamforce.thanksapp.presentation.fragment.challenges.category

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryItem(
    val id: Int,
    val name: String,
    val isChecked: Boolean = false,
    val parentId: Int? = null,
    @SerializedName("children")
    val categories: List<CategoryItem> = emptyList(),
) : DisplayableItem, Parcelable