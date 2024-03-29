package com.teamforce.thanksapp.domain.repositories

import com.teamforce.thanksapp.data.entities.employees.DepartmentFilterEntity
import com.teamforce.thanksapp.data.entities.employees.EmployeeEntity
import com.teamforce.thanksapp.data.request.GetEmployeesRequest
import com.teamforce.thanksapp.utils.ResultWrapper

interface EmployeeRepository {

    suspend fun getEmployees(body: GetEmployeesRequest): List<EmployeeEntity>

    suspend fun getDepartmentTree(): ResultWrapper<List<DepartmentFilterEntity>>
}