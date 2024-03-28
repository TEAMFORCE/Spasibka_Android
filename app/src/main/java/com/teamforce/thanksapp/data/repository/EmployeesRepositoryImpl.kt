package com.teamforce.thanksapp.data.repository

import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.employees.DepartmentFilterEntity
import com.teamforce.thanksapp.data.entities.employees.EmployeeEntity
import com.teamforce.thanksapp.data.request.GetEmployeesRequest
import com.teamforce.thanksapp.domain.repositories.EmployeeRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class EmployeesRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi
    // Маппер
) : EmployeeRepository {


    override suspend fun getEmployees(
      body: GetEmployeesRequest
    ): List<EmployeeEntity> {
        return thanksApi.getColleagues(body = body)
    }

    override suspend fun getDepartmentTree(): ResultWrapper<List<DepartmentFilterEntity>> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.getDepartmentsTree()
        }
    }
}