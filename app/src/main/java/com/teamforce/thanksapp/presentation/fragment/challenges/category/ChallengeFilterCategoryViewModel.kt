package com.teamforce.thanksapp.presentation.fragment.challenges.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeFilterCategoryViewModel @Inject constructor(
    private val categoryInteractor: CategoryInteractor,
    app: Application,
) : AndroidViewModel(app) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    private var initData: List<CategoryItem> = emptyList()
    private val _data = MutableLiveData<List<CategoryItem>>()
    val data: LiveData<List<CategoryItem>> = _data

    private val _backEvent = MutableLiveData<Boolean>()
    val backEvent: LiveData<Boolean> = _backEvent

    private val _pageTitle = MutableLiveData<String>()
    val pageTitle: LiveData<String> = _pageTitle

    private val _onApplyFilters = MutableLiveData<List<CategoryItem>>()
    val onApplyFilters: LiveData<List<CategoryItem>> = _onApplyFilters

    fun onItemClick(item: CategoryItem) {
        _data.value = item.categories
        updateTitle(item)
    }

    private fun updateTitle(item: CategoryItem) {
        _pageTitle.value = item.name
    }

    fun loadCategory(sectionScope: ScopeRequestParams = ScopeRequestParams.TEMPLATES, categoryIds: List<Int>? = null) {
        viewModelScope.launch {
            categoryInteractor.getSections(sectionScope).toResultState(
                onSuccess = {
                    updateData(it, categoryIds)
                }
            )
        }
    }

    fun removeSections(sectionId: Int, section: ScopeRequestParams?) {
        viewModelScope.launch {
            categoryInteractor.removeSections(sectionId)
            section?.let { loadCategory(section) }
        }
    }

    private fun updateData(items: List<CategoryItem>, categoryIds: List<Int>? = null) {

        if (categoryIds?.isNotEmpty() == true) {
            initData = items
            categoryIds.forEach {
                initData = initData.updateCheckedState(it, true)
            }
        } else {
            initData = items
        }

        _data.value = initData
    }

    fun backToParentCategory(defaultTitle: String = "") {
        _data.value?.firstOrNull()?.parentId?.let {
            initData.findLayerById(it)?.let { items ->
                _data.value = items
            }
            initData.findParent(it).let { items ->
                _pageTitle.value = items?.name ?: defaultTitle
            }
        } ?: run {
            _backEvent.value = true
        }
    }

    fun onCheckClick(categoryItem: CategoryItem) {
        initData = initData.updateCheckedState(categoryItem.id, !categoryItem.isChecked)
        initData.findLayerById(categoryItem.id)?.let { items ->
            _data.value = items
        }
    }

    fun addCategory(selectedCategory: CategoryItem?, text: String, section: ScopeRequestParams) {
        viewModelScope.launch {
            categoryInteractor.createSections(
                sectionName = text,
                section = section,
                parentSectionsIds = listOfNotNull(selectedCategory?.id)
            ).toResultState(
                onSuccess = {
                    loadCategory(section)
                }
            )
        }
    }

    fun getSelected(): List<CategoryItem> {
        return initData.allChildren().filter { it.isChecked }
    }
}

fun List<CategoryItem>.allChildren(): List<CategoryItem> =
    flatMap { it.categories.allChildren().plus(it) }
