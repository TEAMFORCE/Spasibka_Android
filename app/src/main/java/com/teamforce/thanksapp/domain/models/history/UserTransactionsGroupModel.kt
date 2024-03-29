package com.teamforce.thanksapp.domain.models.history

import java.util.*

sealed class UserTransactionsGroupModel {

    data class UserGroupModel(
        val userId: Long,
        val type: HistoryItemModel.UserTransactionsModel.TransactionClass,
        val typeName: String?,
        val received: Int,
        val waiting: Int,
        val ready: Int,
        val date: String,
        val userPhoto: String?,
        val name: String?,
        val tgName: String?,
        val nickname: String?
    ): UserTransactionsGroupModel()


    data class DateTimeSeparator(
        val date: String,
        val uuid: UUID = UUID.randomUUID()
    ): UserTransactionsGroupModel()
}
