package com.teamforce.thanksapp.domain.models.notifications

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName


sealed class NotificationItem {
    data class NotificationModel(
        val id: Int,
        val theme: String,
        val isRead: Boolean,
        val createdAt: String,
        val updatedAt: String,
        val data: NotificationAdditionalData,
    ) : NotificationItem()

    data class DateTimeSeparator(
        val date: ResourceWrapper? = null,
        val dateTitle: DateTitle? = null,
    ) : NotificationItem() {
        data class DateTitle(
            val dayText: ResourceWrapper,
            val monthText: ResourceWrapper,
        )
    }
}

sealed interface ResourceWrapper {
    abstract fun toText(context: Context): CharSequence?

    sealed interface Text : ResourceWrapper {
        class Resource(@StringRes val res: Int) : Text {
            override fun toText(context: Context): CharSequence = context.getString(res)
        }

        class Strings(val text: String) : Text {
            override fun toText(context: Context) = text
        }
    }
}


sealed class NotificationAdditionalData {
    data class NotificationTransactionDataModel(
        val amount: Int,
        val status: String,
        val senderId: String?,
        val recipientId: Int,
        val senderNameOrTg: String,
        val recipientTgName: String,
        val senderPhoto: String?,
        val recipientPhoto: String?,
        val transactionId: Int,
        val incomeTransaction: Boolean,
    ) : NotificationAdditionalData()

    data class NotificationChallengeDataModel(
        val challengeId: Int,
        val challengeName: String,
        val creatorTgName: String,
        val creatorFirstName: String,
        val creatorSurname: String,
        val creatorPhoto: String?,
    ) : NotificationAdditionalData()


    data class NotificationChallengeReportDataModel(
        val reportId: Int,
        val challengeId: Int,
        val challengeName: String,
        val reportSenderPhoto: String?,
        val reportSenderNameOrTg: String,
    ) : NotificationAdditionalData()


    data class NotificationChallengeWinnerDataModel(
        val prize: Int,
        val challengeId: Int,
        val challengeName: String,
        val challengeReportId: Int,
    ) : NotificationAdditionalData()

    data class NotificationReactionDataModel(
        val transactionId: Int?,
        val commentId: Int?,
        val challengeId: Int?,
        val reactionFromPhoto: String?,
        val reactionFromNameOrTg: String
    ) : NotificationAdditionalData()

    data class NotificationCommentDataModel(
        val transactionId: Int?,
        val challengeId: Int?,
        val commentFromPhoto: String?,
        val commentFromNameOrTg: String,
    ) : NotificationAdditionalData()

    data class NotificationNewOrderDataModel(
        val orderId: Int,
        val offerId: Int?,
        val customerId: Int,
        val customerName: String,
        val offerName: String,
        val marketplaceId: Int,
        val marketplaceName: String,
    ) : NotificationAdditionalData()

    data class NotificationChallengeEndingDataModel(
        val challengeId: Int,
        val challengeName: String,
    ) : NotificationAdditionalData()

    data class NotificationQuestionnaireDataModel(
        val mode: QuestionnaireMode?,
        val finishedAt: String?,
        val id: Long,
        val title: String
    ) : NotificationAdditionalData() {
        enum class QuestionnaireMode(mode: Int){
            CLOSED_NOW(1),
            CLOSING_TO_ADMIN(2),
            CLOSING_TO_PARTICIPANT(3),
            CREATED(4);
            companion object {
                /**
                 * Возвращает экземпляр [QuestionnaireMode] для заданного значения [mode].
                 *
                 * @param mode Значение, представляющее режим анкеты.
                 * @return Экземпляр [QuestionnaireMode] для указанного значения [mode] или null,
                 * если значение [mode] не соответствует ни одному режиму.
                 */
                fun valueOf(mode: Int): QuestionnaireMode? {
                    return when (mode) {
                        1 -> CLOSED_NOW
                        2 -> CLOSING_TO_ADMIN
                        3 -> CLOSING_TO_PARTICIPANT
                        4 -> CREATED
                        else -> null
                    }
                }
            }
        }

    }

    object Unknown : NotificationAdditionalData()
}

enum class NotificationType {
    Transaction,
    Challenge,
    ChallengeEnding,
    Comment,
    Like,
    ChallengeWinner,
    Report,
    NewOrder,
    Questionnaire,
    Unknown
}


