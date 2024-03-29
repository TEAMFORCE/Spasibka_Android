package com.teamforce.thanksapp.presentation.viewmodel.employees

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.teamforce.thanksapp.domain.interactors.EmployeesInteractor
import com.teamforce.thanksapp.domain.models.employees.DepartmentFilterModel
import com.teamforce.thanksapp.domain.models.employees.EmployeeModel
import com.teamforce.thanksapp.utils.toResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeesViewModel @Inject constructor(
    private val employeesInteractor: EmployeesInteractor
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _employees = MutableLiveData<EmployeeModel?>()
    val employees: LiveData<EmployeeModel?> = _employees

    private var _filterMediatorLiveData = MediatorLiveData<String>()
    val filterMediatorLiveData: MediatorLiveData<String> = _filterMediatorLiveData

    private var _selectedDepartments = MutableLiveData<Set<Long>>()
    val selectedDepartments  = _selectedDepartments

    private var _searchString = MutableLiveData<String>()
    val searchString = _searchString

    private var _isFired = MutableLiveData<Boolean>()
    val isFired = _isFired

    private var _onHoliday = MutableLiveData<Boolean>()
    val onHoliday = _onHoliday

    private var _inOffice = MutableLiveData<Boolean>()
    val inOffice = _inOffice

    private val _employeesError = MutableLiveData<String>()
    val employeesError: LiveData<String> = _employeesError

    private val _internetError = MutableLiveData<Boolean>()
    val internetError: LiveData<Boolean> = _internetError

    fun setSelectedDepartments(value: Set<Long>){
        _selectedDepartments.postValue(value)
    }

    fun setSearchString(value: String?){
        searchString.postValue(value ?: "")
    }

    fun setInOffice(value: Boolean){
        _inOffice.postValue(value)
    }

    fun setIsFired(value: Boolean){
        _isFired.postValue(value)
    }

    fun setOnHoliday(value: Boolean){
        _onHoliday.postValue(value)
    }

    init {
        _filterMediatorLiveData.addSource(
            selectedDepartments
        ){ value ->
            _filterMediatorLiveData.value = value.toString()
        }

        _filterMediatorLiveData.addSource(
            searchString
        ){ value ->
            _filterMediatorLiveData.value = value.toString()
        }

        _filterMediatorLiveData.addSource(
            isFired
        ){ value ->
            _filterMediatorLiveData.value = value.toString()
        }

        _filterMediatorLiveData.addSource(
            onHoliday
        ){ value ->
            _filterMediatorLiveData.value = value.toString()
        }

        _filterMediatorLiveData.addSource(
            inOffice
        ){ value ->
            _filterMediatorLiveData.value = value.toString()
        }

    }

    private val _isEmptyResult = MutableLiveData<Boolean>()
    val isEmptyResult: LiveData<Boolean> get() = _isEmptyResult

    fun setIsEmptyResult(boolean: Boolean){
        _isEmptyResult.value = boolean
    }



    fun getEmployees(): Flow<PagingData<EmployeeModel>> {
        return employeesInteractor.loadEmployees(
            nameForSearch = searchString.value,
            jobTitleForSearch = null,
            inOfficeForSearch = inOffice.value ?: false,
            onHolidayForSearch = onHoliday.value ?: false,
            isFired = isFired.value ?: false,
            selectedDepartments = selectedDepartments.value ?: setOf()
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        ).cachedIn(viewModelScope)
    }

    private val _departments = MutableLiveData<List<DepartmentFilterModel>>()
    val departments: LiveData<List<DepartmentFilterModel>> = _departments

    fun loadDepartmentTree(
    ) {
        viewModelScope.launch {
            employeesInteractor.loadDepartmentTree().toResultState(
                onSuccess = {
                    _departments.postValue(it)
                },
                onError = { error, code ->

                },
                onLoading = {

                }
            )
        }
    }

}
