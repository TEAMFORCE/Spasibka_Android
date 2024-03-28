package com.teamforce.thanksapp.data.repository

import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.transaction.SendCoinsSettingsEntity
import com.teamforce.thanksapp.data.response.SendCoinsResponse
import com.teamforce.thanksapp.domain.repositories.TransactionsRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.mapWrapperData
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
) : TransactionsRepository {


    override suspend fun sendCoins(
        recipient: Int, amount: Int,
        reason: String,
        isAnon: Boolean,
        isPublic: Boolean,
        imageFilePart: List<MultipartBody.Part?>?,
        listOfTagsCheckedValues: MutableList<Int>?,
        stickerId: Int?,
    ): ResultWrapper<SendCoinsResponse> {
        val tags = listOfTagsCheckedValues.toString()
            .filter { it.isDigit() }
            .replace("", " ")
            .removeSurrounding(" ")
        return safeApiCall(Dispatchers.IO) {
            thanksApi.sendCoinsWithImage(
                photo = imageFilePart,
                recipient = recipient,
                amount = amount,
                reason = reason,
                is_anonymous = isAnon,
                isPublic = isPublic,
                tags = tags,
                stickerId = stickerId
            )
        }.mapWrapperData { it }
    }

    override suspend fun getSendCoinsSettings(): ResultWrapper<SendCoinsSettingsEntity> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getSendCoinsSettings()
        }.mapWrapperData { it }
    }

}