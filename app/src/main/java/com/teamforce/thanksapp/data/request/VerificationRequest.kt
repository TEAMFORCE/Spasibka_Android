package com.teamforce.thanksapp.data.request

/**
 * Запрос на верификацию.
 *
 * @property type Тип запроса (по умолчанию "authcode").
 * @property code Код верификации, приходит на почту или в телеграм.
 * @property organization_id Идентификатор организации, в которую будет происходить авторизация.
 * @property shared_key Общий ключ приглашения (получается через диплинк приглашения в организацию).
 */
data class VerificationRequest(
    val type: String = "authcode",
    var code: String,
    val organization_id: Int?,
    var shared_key: String? = null
)
