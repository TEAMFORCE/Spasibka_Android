package com.teamforce.thanksapp.presentation.viewmodel.templates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.domain.models.templates.TemplateModel
import com.teamforce.thanksapp.domain.repositories.TemplatesRepository
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryInteractor
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryItem
import com.teamforce.thanksapp.utils.toSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val templatesRepository: TemplatesRepository,
    private val categoryInteractor: CategoryInteractor,
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _sections = MutableLiveData<List<CategoryItem>?>()
    private val sections: LiveData<List<CategoryItem>?> = _sections

    fun loadTemplates(
        scopeOfTemplates: ScopeRequestParams,
        selectedSections: List<Int>,
    ): Flow<PagingData<TemplateModel>> {

        val tt = runBlocking {
            viewModelScope.launch {
                _sections.value = _sections.value ?: categoryInteractor.getSections(
                    ScopeRequestParams.valueOf(
                        scopeOfTemplates.id.toInt()
                    )
                ).toSuccess()

            }
        }

        val allTemplates = templatesRepository.getTemplates(scopeOfTemplates, selectedSections).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        ).cachedIn(viewModelScope).map {
            it.map { it }
        }

        return allTemplates
    }
}