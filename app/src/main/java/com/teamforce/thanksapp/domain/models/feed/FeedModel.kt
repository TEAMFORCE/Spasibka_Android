package com.teamforce.thanksapp.domain.models.feed

import com.google.gson.annotations.SerializedName

sealed class FeedModel(
    open val id: Int,
    open var userLiked: Boolean,
    open var likesAmount: Int,
    open val commentAmount: Int
) {
    data class ChallengeFeedEvent(
        override val id: Int,
        override val commentAmount: Int,
        val eventObjectId: Int?,
        val eventRecordId: Int?,
        override var likesAmount: Int,
        val time: String,
        val userId: Int?,
        val challengeCreatedAt: String?,
        val challengeEndAt: String?,
        val challengeCreatorName: String?,
        val challengeCreatorId: Int,
        val challengeCreatorTgName: String,
        val challengeId: Int,
        val challengeName: String,
        val challengePhoto: String?,
        override var userLiked: Boolean
    ) : FeedModel(id, userLiked, likesAmount, commentAmount)

    data class TransactionFeedEvent(
        override val id: Int,
        override val commentAmount: Int,
        val eventObjectId: Int?,
        val eventRecordId: Int?,
        override var likesAmount: Int,
        val time: String,
        val userId: Int?,
        val transactionAmount: Int,
        val transactionId: Int,
        val transactionIsAnonymous: Boolean,
        val transactionRecipientId: Int,
        val transactionRecipientPhoto: String?,
        val transactionRecipientTgName: String,
        val recipientName: String?,
        val transactionSenderId: Int?,
        val transactionSenderTgName: String,
        val senderName: String?,
        override var userLiked: Boolean,
        val isAnon: Boolean,
        val transactionUpdatedAt: String,
        val transactionTags: List<String>,
        val isWithMe: Boolean,
        val isFromMe: Boolean
    ) : FeedModel(id, userLiked, likesAmount, commentAmount)

    data class WinnerFeedEvent(
        override val id: Int,
        override val commentAmount: Int,
        val eventObjectId: Int?,
        val eventRecordId: Int?,
        override var likesAmount: Int,
        val time: String,
        val userId: Int?,
        val challengeId: Int,
        val challengeName: String,
        val reportId: Int,
        val updatedAt: String,
        override var userLiked: Boolean,
        val winnerName: String?,
        val winnerId: Int,
        val winnerPhoto: String?,
        val winnerTgName: String
    ) : FeedModel(id, userLiked, likesAmount, commentAmount)

    data class PurchaseFeedEvent(
        override val id: Int,
        override val commentAmount: Int,
        override var likesAmount: Int,
        override var userLiked: Boolean,
        val isWithMe: Boolean = false,
        val eventObjectId: Int?,
        val eventRecordId: Int?,
        val time: String,
        val scopeId: Int?,
        val userId: Int?,
        val productId: Long?,
        val productName: String?,
        val marketId: Long?,
        val orderId: Long,
        val createdAt: String?,
        val customerPhoto: String?,
        val customerName: String?,
        val customerId: Int?,
        val customerTgName: String?,
    ) : FeedModel(id, userLiked, likesAmount, commentAmount)


}
