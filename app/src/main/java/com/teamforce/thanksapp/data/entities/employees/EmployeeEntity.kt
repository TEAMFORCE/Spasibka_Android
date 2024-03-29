package com.teamforce.thanksapp.data.entities.employees

import com.google.gson.annotations.SerializedName

data class EmployeeEntity(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("first_name")
    val firstName: String?,
    val surname: String?,
    @SerializedName("tg_name")
    val tgName: String,
    @SerializedName("job_title")
    val jobTitle: String?,
    val photo: String?
)

/*
   "user_id": 1,
    "first_name": "Андрей",
    "surname": "Иванченко",
    "tg_name": "thisisflight",
    "job_title": "Разработчик",
    "photo": "/media/users_photo/e0d207ba1c8848febedbcbf9d00f58fe_thumb.jpg"
 */
