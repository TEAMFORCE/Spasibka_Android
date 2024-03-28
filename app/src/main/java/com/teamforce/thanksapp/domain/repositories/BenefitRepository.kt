package com.teamforce.thanksapp.domain.repositories

import androidx.paging.PagingData
import com.teamforce.thanksapp.data.api.ImageFileData
import com.teamforce.thanksapp.data.entities.benefit.AvailableMarketEntity
import com.teamforce.thanksapp.data.entities.benefit.Category
import com.teamforce.thanksapp.data.entities.benefit.OfferInCart
import com.teamforce.thanksapp.data.entities.benefit.Order
import com.teamforce.thanksapp.data.response.AddBenefitToCartResponse
import com.teamforce.thanksapp.data.response.CancelTransactionResponse
import com.teamforce.thanksapp.data.response.TransactionOffersFromCartToOrderResponse
import com.teamforce.thanksapp.data.response.commons.CommonStatusResponse
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.BenefitModel
import com.teamforce.thanksapp.domain.models.benefit.ReviewModel
import com.teamforce.thanksapp.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import java.text.FieldPosition

interface BenefitRepository {

    fun getOffers(
        idCategory: Int,
        marketId: Int,
        containString: String?,
        minPrice: String?,
        maxPrice: String?
    ): Flow<PagingData<BenefitModel>>

    suspend fun getAvailableMarkets(): ResultWrapper<List<AvailableMarketEntity>>

    suspend fun getOffersById(offerId: Int, marketId: Int): ResultWrapper<BenefitItemByIdModel>

    suspend fun loadBenefitCategories(marketId: Int): ResultWrapper<List<Category>>

    suspend fun addBenefitToCart(
        offerId: Int,
        marketId: Int,
        quantity: Int,
        position: Int,
        isCheckedStatus: String?
    ): ResultWrapper<AddBenefitToCartResponse>

    fun loadOffersInCart(marketId: Int): Flow<PagingData<OfferInCart>>

    suspend fun loadTotalPriceInCart(marketId: Int): ResultWrapper<Int>

    suspend fun transactionOffersFromCartToOrder(marketId: Int): ResultWrapper<TransactionOffersFromCartToOrderResponse>

    suspend fun deleteOfferInCart(
        orderId: Int,
        marketId: Int
    ): ResultWrapper<CancelTransactionResponse>

    fun getOrdersInHistory(listOfStatus: List<String>?, marketId: Int): Flow<PagingData<Order>>

    // Изолировать понятным enum c ui, и логику в репозитории захендлить
    fun getReviews(
        marketId: Int,
        offerId: Long,
        orderBy: String?,
        reverseOutput: Boolean
    ): Flow<PagingData<ReviewModel>>

    suspend fun sendReview(
        marketId: Int,
        offerId: Long,
        imagesFilePart: List<MultipartBody.Part?>?,
        text: String,
        rate: Int
    ): ResultWrapper<CommonStatusResponse>

    suspend fun deleteReview(marketId: Int, reviewId: Long): ResultWrapper<CommonStatusResponse>

    suspend fun updateReview(
        marketId: Int,
        reviewId: Long,
        imagesFilePart: List<MultipartBody.Part?>?,
        fileList: List<ImageFileData>? = null,
        text: String,
        rate: Int
    ): ResultWrapper<CommonStatusResponse>

}