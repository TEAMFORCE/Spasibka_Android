package com.teamforce.thanksapp.data.request
/**
 * Запрос на смену организации.
 *
 * @property organization_id Идентификатор новой организации для перехода.
 * @property access_token Токен вк, если он есть, то переход произойдет без дополнительного ввода кода(верификации)
 */
data class ChangeOrgRequest(
    val organization_id: Int,
    val access_token: String? = null
)
