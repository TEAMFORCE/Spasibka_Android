package com.teamforce.thanksapp.domain.mappers.history

import com.teamforce.thanksapp.data.entities.history.UserTransactionGroupEntity
import com.teamforce.thanksapp.data.response.HistoryItem
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.domain.models.history.UserTransactionsGroupModel
import com.teamforce.thanksapp.utils.UserDataRepository
import javax.inject.Inject

class HistoryMapper @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {

    fun mapGroupList(from: List<UserTransactionGroupEntity>): List<UserTransactionsGroupModel.UserGroupModel>{
        return from.map ( ::mapGroup )
    }

    private fun mapGroup(from: UserTransactionGroupEntity): UserTransactionsGroupModel.UserGroupModel {
        return UserTransactionsGroupModel.UserGroupModel(
            userId = from.userId,
            userPhoto = from.userPhoto,
            tgName = from.tgName,
            nickname = from.nickname,
            name = "${from.firstName} ${from.surname}",
            received = from.received,
            ready = from.ready,
            waiting = from.waiting,
            date = from.date,
            type = mapTransactionClass(from.type),
            typeName = from.typeName
        )
    }

    fun mapList(from: List<HistoryItem.UserTransactionsResponse>): List<HistoryItemModel.UserTransactionsModel>{
        return from.map(::map)
    }


    fun map(from: HistoryItem.UserTransactionsResponse): HistoryItemModel.UserTransactionsModel {
        return HistoryItemModel.UserTransactionsModel(
            id = from.id,
            sender = mapSender(from.sender),
            sender_id = from.sender_id,
            recipient = mapRecipient(from.recipient),
            recipient_id = from.recipient_id,
            transaction_status = mapTransactionStatus(from.transaction_status),
            transactionClass = mapTransactionClass(from.transactionClass.id),
            expireToCancel = from.expireToCancel,
            amount = from.amount,
            canUserCancel = from.canUserCancel,
            tags = from.tags,
            createdAt = from.createdAt,
            updatedAt = from.updatedAt,
            reason = from.reason,
            photo = from.photo,
            photos = from.photos,
            sticker = from.sticker,
            iAmSender = userDataRepository.getProfileId()?.toInt() == from.sender?.sender_id
            )
    }

    private fun mapSender(from: HistoryItem.UserTransactionsResponse.Sender?): HistoryItemModel.UserTransactionsModel.Sender? {
        from?.let {
            return HistoryItemModel.UserTransactionsModel.Sender(
                sender_id = from.sender_id,
                sender_tg_name = from.sender_tg_name,
                sender_surname = from.sender_surname,
                sender_first_name = from.sender_first_name,
                sender_photo = from.sender_photo,
                challenge_id = from.challenge_id,
                challenge_name = from.challenge_name
            )
        }
        return null

    }

    private fun mapRecipient(from: HistoryItem.UserTransactionsResponse.Recipient?): HistoryItemModel.UserTransactionsModel.Recipient? {
        from?.let {
            return HistoryItemModel.UserTransactionsModel.Recipient(
                recipient_id = from.recipient_id,
                recipient_tg_name = from.recipient_tg_name,
                recipient_surname = from.recipient_surname,
                recipient_first_name = from.recipient_first_name,
                recipient_photo = from.recipient_photo
            )
        }
        return null

    }

    private fun mapTransactionStatus(from: HistoryItem.UserTransactionsResponse.TransactionStatus): HistoryItemModel.UserTransactionsModel.TransactionStatus{
        return when(from.id){
            "W" -> HistoryItemModel.UserTransactionsModel.TransactionStatus.WAITING
            "A" -> HistoryItemModel.UserTransactionsModel.TransactionStatus.APPROVED
            "D" -> HistoryItemModel.UserTransactionsModel.TransactionStatus.DECLINED
            "G" -> HistoryItemModel.UserTransactionsModel.TransactionStatus.INGRACE
            "R" -> HistoryItemModel.UserTransactionsModel.TransactionStatus.READY
            "C" -> HistoryItemModel.UserTransactionsModel.TransactionStatus.CANCELLED
            else -> HistoryItemModel.UserTransactionsModel.TransactionStatus.UNKNOW
        }
    }

    private fun mapTransactionClass(from: String): HistoryItemModel.UserTransactionsModel.TransactionClass {
        return when(from){
            "T" -> HistoryItemModel.UserTransactionsModel.TransactionClass.THANKS
            "H" -> HistoryItemModel.UserTransactionsModel.TransactionClass.CONTRIBUTION_TO_CHALLENGE
            "W" -> HistoryItemModel.UserTransactionsModel.TransactionClass.REWARD_FOR_CHALLENGE
            "F" -> HistoryItemModel.UserTransactionsModel.TransactionClass.REFUND_FROM_CHALLENGE
            "D", "M" -> HistoryItemModel.UserTransactionsModel.TransactionClass.THANKS_FROM_SYSTEM
            "B" -> HistoryItemModel.UserTransactionsModel.TransactionClass.FIRED_THANKS
            "I" -> HistoryItemModel.UserTransactionsModel.TransactionClass.REFUND_FROM_BENEFIT
            else -> HistoryItemModel.UserTransactionsModel.TransactionClass.THANKS
        }
    }

}