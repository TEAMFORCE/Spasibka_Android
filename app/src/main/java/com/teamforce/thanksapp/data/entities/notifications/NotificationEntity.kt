package com.teamforce.thanksapp.data.entities.notifications

import com.google.gson.annotations.SerializedName

data class NotificationEntity(
    @SerializedName("id")
    val id: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("theme")
    val theme: String?,
    @SerializedName("read")
    val isRead: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("transaction_data")
    val transactionData: NotificationTransactionDataEntity?,
    @SerializedName("challenge_data")
    val challengeData: NotificationChallengeDataEntity?,
    @SerializedName("report_data")
    val reportData: NotificationChallengeReportData?,
    @SerializedName("comment_data")
    val commentData: NotificationCommentData?,
    @SerializedName("like_data")
    val likeData: NotificationReactionData?,
    @SerializedName("winner_data")
    val winnerData: NotificationChallengeWinnerData?,
    @SerializedName("order_data")
    val orderData: NotificationNewOrderData?,
    @SerializedName("challenge_ending_data")
    val challengeEndingData: NotificationChallengeEnding?,
    @SerializedName("questionnaire_data")
    val questionnaireData : NotificationQuestionnaireData?
)

data class NotificationTransactionDataEntity(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("sender_id")
    val senderId: String?,
    @SerializedName("recipient_id")
    val recipientId: Int,
    @SerializedName("sender_tg_name")
    val senderTgName: String?,
    @SerializedName("sender_surname")
    val senderSurname: String?,
    @SerializedName("sender_firstname")
    val senderFirstName: String?,
    @SerializedName("recipient_tg_name ")
    val recipientTgName: String?,
    @SerializedName("sender_photo")
    val senderPhoto: String?,
    @SerializedName("recipient_photo")
    val recipientPhoto: String?,
    @SerializedName("transaction_id")
    val transactionId: Int,
    @SerializedName("income_transaction")
    val incomeTransaction: Boolean,
)

data class NotificationChallengeDataEntity(
    @SerializedName("challenge_id")
    val challengeId: Int,
    @SerializedName("challenge_name")
    val challengeName: String?,
    @SerializedName("creator_tg_name")
    val creatorTgName: String?,
    @SerializedName("creator_first_name")
    val creatorFirstName: String?,
    @SerializedName("creator_surname")
    val creatorSurname: String?,
    @SerializedName("creator_photo")
    val creatorPhoto: String?
)

data class NotificationChallengeReportData(
    @SerializedName("report_id")
    val reportId: Int,
    @SerializedName("challenge_id")
    val challengeId: Int,
    @SerializedName("challenge_name")
    val challengeName: String?,
    @SerializedName("report_sender_photo")
    val reportSenderPhoto: String?,
    @SerializedName("report_sender_surname")
    val reportSenderSurname: String?,
    @SerializedName("report_sender_tg_name")
    val reportSenderTgName: String?,
    @SerializedName("report_sender_first_name")
    val reportSenderFirstName: String?
)

data class NotificationChallengeWinnerData(
    @SerializedName("prize")
    val prize: Int,
    @SerializedName("challenge_id")
    val challengeId: Int,
    @SerializedName("challenge_name")
    val challengeName: String?,
    @SerializedName("challenge_report_id")
    val challengeReportId: Int
)

data class NotificationReactionData(
    @SerializedName("transaction_id")
    val transactionId: Int?,
    @SerializedName("comment_id ")
    val commentId: Int?,
    @SerializedName("challenge_id")
    val challengeId: Int?,
    @SerializedName("reaction_from_photo")
    val reactionFromPhoto: String?,
    @SerializedName("reaction_from_tg_name")
    val reactionFromTgName: String?,
    @SerializedName("reaction_from_surname")
    val reactionFromSurname: String?,
    @SerializedName("reaction_from_first_name")
    val reactionFromFirstName: String?
)

data class NotificationCommentData(
    @SerializedName("transaction_id")
    val transactionId: Int?,
    @SerializedName("challenge_id")
    val challengeId: Int?,
    @SerializedName("comment_from_photo")
    val commentFromPhoto: String?,
    @SerializedName("comment_from_tg_name")
    val commentFromTgName: String?,
    @SerializedName("comment_from_surname")
    val commentFromSurname: String?,
    @SerializedName("comment_from_first_name")
    val commentFromFirstName: String?

)

data class NotificationNewOrderData(
    @SerializedName("order_id")
    val orderId: Int,
    @SerializedName("offer_id")
    val offerId: Int?,
    @SerializedName("customer_id")
    val customerId: Int,
    @SerializedName("customer_name")
    val customerName: String,
    @SerializedName("offer_name")
    val offerName: String,
    @SerializedName("marketplace_id")
    val marketplaceId: Int,
    @SerializedName("marketplace_name")
    val marketplaceName: String,
)

data class NotificationChallengeEnding(
    @SerializedName("challenge_id")
    val challengeId: Int,
    @SerializedName("challenge_name")
    val challengeName: String?,
)

data class NotificationQuestionnaireData(
    val mode: Int,
    @SerializedName("finished_at")
    val finishedAt: String?,
    @SerializedName("questionnaire_id")
    val id: Long,
    @SerializedName("questionnaire_title")
    val title: String
)