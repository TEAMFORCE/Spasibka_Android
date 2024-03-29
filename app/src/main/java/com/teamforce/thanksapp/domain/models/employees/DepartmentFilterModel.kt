package com.teamforce.thanksapp.domain.models.employees


data class DepartmentFilterModel(
    val id: Long,
    val name: String,
    val children: List<DepartmentFilterModel>
)
