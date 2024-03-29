package com.teamforce.thanksapp.presentation.fragment.challenges.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedCategoryViewModel @Inject constructor(
) : ViewModel() {

    private val _onApplySelections = MutableLiveData<List<CategoryItem>>()
    val onApplySelections: LiveData<List<CategoryItem>> = _onApplySelections

    fun applySelection(value: List<CategoryItem>) {
        _onApplySelections.value = value
    }

    fun getSelected(): List<Int> = _onApplySelections.value?.map { it.id } ?: emptyList()
    fun clearSelected() {
        _onApplySelections.value = emptyList()
    }

    fun getSelectedWithName(): List<String> = _onApplySelections.value?.map { it.name } ?: emptyList()
}

