package com.teamforce.thanksapp.domain.mappers.notifications

import com.teamforce.thanksapp.data.entities.notifications.*
import com.teamforce.thanksapp.domain.models.notifications.NotificationAdditionalData
import com.teamforce.thanksapp.domain.models.notifications.NotificationItem
import com.teamforce.thanksapp.domain.models.notifications.NotificationType
import com.teamforce.thanksapp.utils.username
import javax.inject.Inject

class NotificationMapper @Inject constructor(
) {
    fun map(from: NotificationEntity): NotificationItem.NotificationModel {
        return NotificationItem.NotificationModel(
            theme = from.theme ?: "Unknown",
            isRead = from.isRead,
            createdAt = from.createdAt,
            updatedAt = from.updatedAt,
            id = from.id,
            data = when (mapType(from.type)) {
                NotificationType.Transaction -> mapNotificationTransactionData(from.transactionData!!)
                NotificationType.Challenge -> mapNotificationChallengeData(from.challengeData!!)
                NotificationType.Comment -> mapNotificationCommentData(from.commentData!!)
                NotificationType.Report -> mapNotificationChallengeReportData(from.reportData!!)
                NotificationType.ChallengeWinner -> mapNotificationChallengeWinnerData(from.winnerData!!)
                NotificationType.Like -> mapNotificationReactionData(from.likeData!!)
                NotificationType.NewOrder -> mapNotificationNewOrderData(from.orderData!!)
                NotificationType.ChallengeEnding -> mapNotificationChallengeEndingData(from.challengeEndingData!!)
                NotificationType.Questionnaire -> mapNotificationQuestionnaireData(from.questionnaireData!!)
                else -> NotificationAdditionalData.Unknown
            }
        )

    }

    private fun mapType(stringType: String): NotificationType {
        return when (stringType.lowercase()) {
            "t" -> NotificationType.Transaction
            "h" -> NotificationType.Challenge
            "c" -> NotificationType.Comment
            "l" -> NotificationType.Like
            "w" -> NotificationType.ChallengeWinner
            "r" -> NotificationType.Report
            "m" -> NotificationType.NewOrder
            "e" -> NotificationType.ChallengeEnding
            "q" -> NotificationType.Questionnaire
            else -> NotificationType.Unknown
        }
    }

    private fun mapNotificationTransactionData(from: NotificationTransactionDataEntity): NotificationAdditionalData.NotificationTransactionDataModel {
        return NotificationAdditionalData.NotificationTransactionDataModel(
            amount = from.amount,
            status = from.status,
            senderId = from.senderId,
            recipientId = from.recipientId,
            senderNameOrTg = if (from.senderFirstName.isNullOrEmpty() && from.senderSurname.isNullOrEmpty()) from.senderTgName?.username()
                ?: "Unknow" else "${from.senderFirstName} ${from.senderSurname}",
            recipientTgName = from.recipientTgName ?: "Unknown",
            senderPhoto = from.senderPhoto,
            recipientPhoto = from.recipientPhoto,
            transactionId = from.transactionId,
            incomeTransaction = from.incomeTransaction
        )
    }

    private fun mapNotificationChallengeData(from: NotificationChallengeDataEntity): NotificationAdditionalData.NotificationChallengeDataModel {
        return NotificationAdditionalData.NotificationChallengeDataModel(
            challengeId = from.challengeId,
            challengeName = from.challengeName ?: "Unknown",
            creatorTgName = from.creatorTgName ?: "",
            creatorFirstName = from.creatorFirstName ?: "",
            creatorSurname = from.creatorSurname ?: "",
            creatorPhoto = from.creatorPhoto
        )
    }

    private fun mapNotificationChallengeReportData(from: NotificationChallengeReportData): NotificationAdditionalData.NotificationChallengeReportDataModel {
        return NotificationAdditionalData.NotificationChallengeReportDataModel(
            reportId = from.reportId,
            challengeId = from.challengeId,
            challengeName = from.challengeName ?: "Unknown",
            reportSenderPhoto = from.reportSenderPhoto,
            reportSenderNameOrTg = if (from.reportSenderFirstName.isNullOrEmpty() && from.reportSenderSurname.isNullOrEmpty()) from.reportSenderTgName?.username()
                ?: "Unknow" else "${from.reportSenderFirstName} ${from.reportSenderSurname}"
        )
    }

    private fun mapNotificationChallengeWinnerData(from: NotificationChallengeWinnerData): NotificationAdditionalData.NotificationChallengeWinnerDataModel {
        return NotificationAdditionalData.NotificationChallengeWinnerDataModel(
            prize = from.prize,
            challengeId = from.challengeId,
            challengeName = from.challengeName ?: "Unknown",
            challengeReportId = from.challengeReportId
        )
    }

    private fun mapNotificationReactionData(from: NotificationReactionData): NotificationAdditionalData.NotificationReactionDataModel {
        return NotificationAdditionalData.NotificationReactionDataModel(
            transactionId = from.transactionId,
            commentId = from.commentId,
            challengeId = from.challengeId,
            reactionFromPhoto = from.reactionFromPhoto,
            reactionFromNameOrTg = if (from.reactionFromFirstName.isNullOrEmpty() && from.reactionFromSurname.isNullOrEmpty()) from.reactionFromTgName?.username()
                ?: "Unknow" else "${from.reactionFromFirstName} ${from.reactionFromSurname}"
        )
    }

    private fun mapNotificationCommentData(from: NotificationCommentData): NotificationAdditionalData.NotificationCommentDataModel {
        return NotificationAdditionalData.NotificationCommentDataModel(
            transactionId = from.transactionId,
            challengeId = from.challengeId,
            commentFromPhoto = from.commentFromPhoto,
            commentFromNameOrTg = if (from.commentFromFirstName.isNullOrEmpty() && from.commentFromSurname.isNullOrEmpty()) from.commentFromTgName?.username()
                ?: "Unknow" else "${from.commentFromFirstName} ${from.commentFromSurname}"
        )
    }

    private fun mapNotificationNewOrderData(from: NotificationNewOrderData): NotificationAdditionalData.NotificationNewOrderDataModel {
        return NotificationAdditionalData.NotificationNewOrderDataModel(
            orderId = from.orderId,
            customerId = from.customerId,
            customerName = from.customerName,
            offerName = from.offerName,
            marketplaceId = from.marketplaceId,
            marketplaceName = from.marketplaceName,
            offerId = from.offerId
        )
    }

    private fun mapNotificationChallengeEndingData(from: NotificationChallengeEnding): NotificationAdditionalData.NotificationChallengeEndingDataModel {
        return NotificationAdditionalData.NotificationChallengeEndingDataModel(
            challengeId = from.challengeId,
            challengeName = from.challengeName ?: ""
        )
    }

    private fun mapNotificationQuestionnaireData(from: NotificationQuestionnaireData): NotificationAdditionalData.NotificationQuestionnaireDataModel {
        return NotificationAdditionalData.NotificationQuestionnaireDataModel(
            mode = NotificationAdditionalData.NotificationQuestionnaireDataModel.QuestionnaireMode.valueOf(
                from.mode
            ),
            finishedAt = from.finishedAt,
            title = from.title,
            id = from.id
        )
    }

}