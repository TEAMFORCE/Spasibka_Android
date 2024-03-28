package com.teamforce.thanksapp.data.sources.employees

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.employees.EmployeeEntity
import com.teamforce.thanksapp.data.request.GetEmployeesRequest
import com.teamforce.thanksapp.domain.mappers.employees.EmployeeMapper
import com.teamforce.thanksapp.domain.models.employees.EmployeeModel
import com.teamforce.thanksapp.domain.repositories.EmployeeRepository
import com.teamforce.thanksapp.utils.Consts
import retrofit2.HttpException
import java.io.IOException

class EmployeesPagingSource(
    private val api: EmployeeRepository,
    private val employeesMapper: EmployeeMapper,
    private val nameForSearch: String?,
    private val jobTitleForSearch: String?,
    private val inOfficeForSearch: Int?,
    private val onHolidayForSearch: Int?,
    private val isFired: Int?,
    private val selectedDepartments: Set<Long>
) : PagingSource<Int, EmployeeModel>() {

    override fun getRefreshKey(state: PagingState<Int, EmployeeModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: PagingSource.LoadParams<Int>): PagingSource.LoadResult<Int, EmployeeModel> {
        var pageIndex = params.key ?: 1

        if (params is PagingSource.LoadParams.Refresh) {
            pageIndex = 1
        }

        return try {
            val response = api.getEmployees(
               body = GetEmployeesRequest(
                   name = nameForSearch,
                   jobTitle = jobTitleForSearch,
                   inOffice = inOfficeForSearch,
                   onHoliday = onHolidayForSearch,
                   firedAt = isFired,
                   offset = pageIndex,
                   limit = Consts.PAGE_SIZE,
                   departments = selectedDepartments.toList()
               )
            )
            val nextKey =
                if (response.isEmpty()) null
                else {
                    pageIndex + (params.loadSize / Consts.PAGE_SIZE)
                }
            PagingSource.LoadResult.Page(
                data = employeesMapper.mapList(response),
                prevKey = if (pageIndex == 1) null else pageIndex,
                nextKey = nextKey
            )

        } catch (exception: IOException) {
            return PagingSource.LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return PagingSource.LoadResult.Error(exception)
        }
    }
}
