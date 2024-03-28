package com.teamforce.thanksapp.data.entities.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Сущность профиля
 *
 * @property username Тг или почта пользователя
 * @property profile внутр инфа профиля
 * @property rate Индекс внутренней актвиности
 * @property privileged Роли пользователя, департаменты
 */
@Parcelize
data class ProfileEntity(
    val id: Int,
    @SerializedName("username")
    val username: String?,
    @SerializedName("profile")
    val profile: ProfileBeanEntity,
    val rate: Int,
    val privileged: List<PrivilegedEntity>?
) : Parcelable

@Parcelize
data class PrivilegedEntity(
    @SerializedName("role_name")
    val roleName: String?,
    @SerializedName("role")
    val roleChar: String?,
    @SerializedName("department_name")
    val departmentName: String?
) : Parcelable

