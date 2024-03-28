package com.teamforce.thanksapp.presentation.fragment.challenges.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedFilterCategoryViewModel @Inject constructor() : ViewModel() {

    private val _onApplyFilters = MutableLiveData<List<CategoryItem>>()
    val onApplyFilters: LiveData<List<CategoryItem>> = _onApplyFilters

    fun onApplyFilters(categoryItems: List<CategoryItem>) {
        _onApplyFilters.value = categoryItems
    }

    fun clearFilters() {
        _onApplyFilters.value = emptyList()
    }
}

