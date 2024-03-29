package com.teamforce.thanksapp.data.response

import com.google.gson.annotations.SerializedName

/**
 * Сущность баланса
 *
 * @property income Счет заработанных(для трат на бенефиты)
 * @property distribute Счет для благодарностей
 */
data class BalanceResponse(
    val income: IncomeBean,
    @SerializedName("distr")
    val distribute: DistributeBean
) {
    data class IncomeBean(
        val amount: Int,
        val frozen: Int,
        val sent: Int,
        val received: Int,
        val cancelled: Int
    )

    data class DistributeBean(
        val amount: Int,
        @SerializedName("expire_date")
        val expireDate: String?,
        val frozen: Int,
        val sent: Int,
        val received: Int,
        val cancelled: Int
    )
}
