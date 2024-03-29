package com.teamforce.thanksapp.data.request
/**
 * Запрос на авторизацию.
 *
 * @property type Тип запроса (по умолчанию "authcode").
 * @property login Телеграм или почта пользователя.
 * @property shared_key Общий ключ приглашения (получается через диплинк приглашения в организацию).
 */
data class AuthorizationRequest(
    val type: String = "authorize",
    var login: String, // Telegram or Email
    var shared_key: String? = null,
)
