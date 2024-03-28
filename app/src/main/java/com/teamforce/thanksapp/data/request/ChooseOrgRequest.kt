package com.teamforce.thanksapp.data.request
/**
 * Запрос на выбор организации.
 *
 * @property user_id Идентификатор текущего юзера
 * @property organization_id Идентификатор организации, в которую будет происходить авторизация.
 */
data class ChooseOrgRequest(
    val user_id: Int,
    val organization_id: Int?
)
