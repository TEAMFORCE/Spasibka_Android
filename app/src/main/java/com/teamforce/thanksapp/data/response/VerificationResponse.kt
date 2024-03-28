package com.teamforce.thanksapp.data.response

import com.google.gson.annotations.SerializedName

/**
 * Ответ на верификацию, после запроса на смену организации.
 *
 * @property type Откуда произошла верификация(тг, почта)
 * @property token Хешированный токен для получения доступа к эндпоинтам
 * @property isSuccess Успешна ли верификация
 * @property reason Словесное описание итога верификации
 */
data class VerificationResponse(
    var type: String = "",
    @SerializedName("is_success") var isSuccess: Boolean = false,
    @SerializedName("token")
    var token: String = "",
    var reason: String = ""
)
