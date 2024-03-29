package com.teamforce.thanksapp.data.response

import com.google.gson.annotations.SerializedName
/**
 * Запрос на верификацию, после запроса на смену организации.
 *
 * @property target Через что происходила атворизауия
 * @property status Словесный статус ответа с бека
 * @property organizations Список организаций, для выбора, в какую именно переходить
 * @property xTelegram Хеш кода, полученного через tg бота
 * @property xEmail Хеш кода, полученного через почту
 * @property xCode Хеш код, кода который ждет бек для верификации
 * @property isTest Для обозначения входа тестового аккаунта(login testapple, code 4444)
 */
data class AuthResponse(
    val target: String,
    val status: String,
    val organizations: List<Organization>?,
    @SerializedName("x-telegram")
    val xTelegram: String?,
    @SerializedName("x-code")
    val xCode: String?,
    @SerializedName("x-email")
    val xEmail: String?,
    val isTest: Boolean = false
){
    data class Organization(
        val user_id: Int,
        val organization_id: Int,
        val organization_name: String,
        val organization_photo: String?,
        val is_current: Boolean
    )

    data class TelegramResponse(
        val target: String?,
        val status: String?,
        @SerializedName("x-telegram")
        val xTelegram: String?,
        @SerializedName("x-code")
        val xCode: String?,
    )

    data class EmailResponse(
        val target: String?,
        val status: String?,
        @SerializedName("x-code")
        val xCode: String?,
        @SerializedName("x-email")
        val xEmail: String?
    )
}
