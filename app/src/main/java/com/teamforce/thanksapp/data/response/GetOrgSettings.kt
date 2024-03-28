package com.teamforce.thanksapp.data.response

import com.google.gson.annotations.SerializedName

/**
 * Сущность настроек организации
 *
 * @property name название организации
 * @property telegramGroup список групп телеграма, привязанных к организации
 * @property hex цвет бренда
 * @property photo логотип организации
 * @property licenseEnd дата окончания периода (срока действия лицензии)
 */
data class GetOrgSettings(
    val name: String,
    @SerializedName("telegram_group")
    val telegramGroup: List<String>,
    val hex: String,
    val photo: String?,
    @SerializedName("license_end")
    val licenseEnd: String?
)
