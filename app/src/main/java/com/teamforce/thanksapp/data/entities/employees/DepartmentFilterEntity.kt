package com.teamforce.thanksapp.data.entities.employees

data class DepartmentFilterEntity(
    val id: Long,
    val name: String,
    val children: List<DepartmentFilterEntity>
)
