package com.teamforce.thanksapp.data.request

import com.google.gson.annotations.SerializedName
/**
 * Запрос на верификацию, после запроса на смену организации.
 *
 * @property code Код верификации, приходит на почту или в телеграм.
 * @property orgId Идентификатор организации, в которую будет происходить верификация.
 * @property xCode Хеш кода, который нужно отправить для верификации(получение от бека после авторизации)
 * @property tgCode Хеш кода, полученного через tg бота
 * @property email Хеш кода, полученного через почту
 */
data class VerificationRequestForChangeOrg(
    val code: String,
    @SerializedName("organization_id")
    val orgId: String,
    @SerializedName("X-Code")
    val xCode: String,
    @SerializedName("tg_id")
    val tgCode: String?,
    val email: String?
)
