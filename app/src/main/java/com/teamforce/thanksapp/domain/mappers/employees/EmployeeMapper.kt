package com.teamforce.thanksapp.domain.mappers.employees

import com.teamforce.thanksapp.data.entities.employees.DepartmentFilterEntity
import com.teamforce.thanksapp.data.entities.employees.EmployeeEntity
import com.teamforce.thanksapp.domain.models.employees.DepartmentFilterModel
import com.teamforce.thanksapp.domain.models.employees.EmployeeModel
import javax.inject.Inject


class EmployeeMapper @Inject constructor(){

    fun map(from: EmployeeEntity): EmployeeModel {
        return EmployeeModel(
            id = from.userId,
            name = "${from.firstName} ${from.surname}",
            tgName = from.tgName,
            photo = from.photo,
            jobTitle = from.jobTitle
        )

    }

    fun mapList(from: List<EmployeeEntity>): List<EmployeeModel> {
        return from.map {
            map(it)
        }
    }

    fun mapDepartmentTree(from: List<DepartmentFilterEntity>): List<DepartmentFilterModel>{
        return from.map { mapDepartmentFilterEntityToModel(it) }
    }

    private fun mapDepartmentFilterEntityToModel(from: DepartmentFilterEntity): DepartmentFilterModel{
        val childrenModels = from.children.map { mapDepartmentFilterEntityToModel(it) }
        return DepartmentFilterModel(from.id, from.name, childrenModels)
    }
}