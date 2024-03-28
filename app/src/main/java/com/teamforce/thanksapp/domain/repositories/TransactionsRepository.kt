package com.teamforce.thanksapp.domain.repositories

import com.teamforce.thanksapp.data.entities.transaction.SendCoinsSettingsEntity
import com.teamforce.thanksapp.data.response.SendCoinsResponse
import com.teamforce.thanksapp.utils.ResultWrapper
import okhttp3.MultipartBody

interface TransactionsRepository {

    suspend fun sendCoins(
        recipient: Int, amount: Int,
        reason: String,
        isAnon: Boolean,
        isPublic: Boolean,
        imageFilePart: List<MultipartBody.Part?>?,
        listOfTagsCheckedValues: MutableList<Int>?,
        stickerId: Int?,
    ): ResultWrapper<SendCoinsResponse>

    suspend fun getSendCoinsSettings(): ResultWrapper<SendCoinsSettingsEntity>
}