package com.teamforce.thanksapp.domain.models.employees


data class EmployeeModel(
    val id: Int,
    val name: String?,
    val tgName: String,
    val jobTitle: String?,
    val photo: String?
)
