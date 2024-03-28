package com.teamforce.thanksapp.domain.models.history

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.model.domain.TagModel
import kotlinx.parcelize.Parcelize
import java.util.*

sealed class HistoryItemModel {
    @Parcelize
    data class UserTransactionsModel(
        val id: Int,
        val sender: Sender?,
        val sender_id: Int?,
        val recipient: Recipient?,
        val recipient_id: Int?,
        val transaction_status: TransactionStatus,
        val transactionClass: TransactionClass,
        val expireToCancel: String?,
        val amount: String,
        val canUserCancel: Boolean?,
        val tags: List<TagModel>?,
        val createdAt: String,
        val updatedAt: String,
        val reason: String?,
        val photo: String?,
        val photos: List<String?>?,
        val sticker: String?,
        val iAmSender: Boolean = false
    ) : HistoryItemModel(), Parcelable {
        @Parcelize
        data class Sender(
            val sender_id: Int?,
            val sender_tg_name: String?,
            val sender_first_name: String?,
            val sender_surname: String?,
            val sender_photo: String?,
            val challenge_name: String?,
            val challenge_id: Int
        ) : Parcelable

        @Parcelize
        data class Recipient(
            val recipient_id: Int,
            val recipient_tg_name: String?,
            val recipient_first_name: String?,
            val recipient_surname: String?,
            val recipient_photo: String?
        ) : Parcelable

        @Parcelize
        enum class TransactionStatus(val value: String): Parcelable{
            WAITING("W"), APPROVED("A"), DECLINED("D"), INGRACE("G"),
            READY("R"), CANCELLED("C"), UNKNOW("U")
        }

        @Parcelize
        enum class TransactionClass(val value: String) : Parcelable {
            THANKS("T"), CONTRIBUTION_TO_CHALLENGE("H"), REWARD_FOR_CHALLENGE("W"),
            REFUND_FROM_CHALLENGE("F"), THANKS_FROM_SYSTEM("D"), FIRED_THANKS("B"), REFUND_FROM_BENEFIT("I")
        }
    }

    /*
    Типы транзакций, которых еще нет на клиенте, но есть на беке
    EXP = 'X', 'За стаж работы'  # один раз в начале периода, идет в account [INCOME] c account [SYSTEM], его баланс не проверяем
    # плюс по команде администратора в account [DISTR] менеджера с account [SYSTEM], баланс проверяем
    REDIST = 'R', 'Для перераспределения'  # между менеджерами, идет в account [DISTR] из account [DISTR|INCOME(если разрешено настройкой)]
    BONUS = 'O', 'Для расчета премии'  # в конце периода, только из account [INCOME, только в account [BONUS]
    PURCHASE = 'P', 'Покупка'  # при покупках в внутреннем маркетплейсе, только из account [INCOMDE] только в account [MARKET]
    EMIT = 'E', 'Эмитирование'  # только для account [SYSTEM] из account [TREASURY], вброс баллов в систему, возможно как погашение отрицательного баланса account [SYSTEM], источника
    CASH = 'C', 'Погашение'  # источник account [BONUS|MARKET|BURNING], получатель account [TREASURY]
    MANUAL = 'M', 'Ручной'
    WITHDRAW = 'A', 'Вывод на биржу'  # на вывод
    RETURN_FROM_WITHDRAW = 'G', 'Возврат с вывода на биржу'  # возврат из вывода (отмена или отказ от вывода)
     */

    data class DateTimeSeparator(
        val date: String,
        val uuid: UUID = UUID.randomUUID()
    ) : HistoryItemModel()
}



