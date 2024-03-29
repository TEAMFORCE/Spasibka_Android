package com.teamforce.thanksapp.domain.interactors

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.teamforce.thanksapp.data.sources.employees.EmployeesPagingSource
import com.teamforce.thanksapp.domain.mappers.employees.EmployeeMapper
import com.teamforce.thanksapp.domain.models.employees.DepartmentFilterModel
import com.teamforce.thanksapp.domain.models.employees.EmployeeModel
import com.teamforce.thanksapp.domain.repositories.EmployeeRepository
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.mapWrapperData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EmployeesInteractor @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val employeeMapper: EmployeeMapper
) {
    fun loadEmployees(
        nameForSearch: String?,
        selectedDepartments: Set<Long>,
        jobTitleForSearch: String?,
        inOfficeForSearch: Boolean,
        onHolidayForSearch: Boolean,
        isFired: Boolean
    ): Flow<PagingData<EmployeeModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 5,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                EmployeesPagingSource(
                    api = employeeRepository,
                    employeesMapper = employeeMapper,
                    nameForSearch = nameForSearch,
                    jobTitleForSearch = jobTitleForSearch,
                    inOfficeForSearch = if(inOfficeForSearch) 1 else 0,
                    onHolidayForSearch = if(onHolidayForSearch) 1 else 0,
                    isFired = if(isFired) 1 else 0,
                    selectedDepartments = selectedDepartments
                )
            }
        ).flow
    }

    suspend fun loadDepartmentTree(): ResultWrapper<List<DepartmentFilterModel>>{
        return employeeRepository.getDepartmentTree().mapWrapperData {
            employeeMapper.mapDepartmentTree(it)
        }
    }
}