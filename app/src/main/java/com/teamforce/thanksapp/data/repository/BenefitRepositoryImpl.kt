package com.teamforce.thanksapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.teamforce.thanksapp.data.api.ImageFileData
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.benefit.AvailableMarketEntity
import com.teamforce.thanksapp.data.entities.benefit.Category
import com.teamforce.thanksapp.data.entities.benefit.OfferInCart
import com.teamforce.thanksapp.data.entities.benefit.Order
import com.teamforce.thanksapp.data.request.AddBenefitToCartRequest
import com.teamforce.thanksapp.data.request.TransactionOffersFromCartToOrderRequest
import com.teamforce.thanksapp.data.response.AddBenefitToCartResponse
import com.teamforce.thanksapp.data.response.CancelTransactionResponse
import com.teamforce.thanksapp.data.response.TransactionOffersFromCartToOrderResponse
import com.teamforce.thanksapp.data.response.commons.CommonStatusResponse
import com.teamforce.thanksapp.data.sources.benefit.BenefitPagingSource
import com.teamforce.thanksapp.data.sources.benefit.HistoryOfOrdersPagingSource
import com.teamforce.thanksapp.data.sources.benefit.ReviewsPagingSource
import com.teamforce.thanksapp.data.sources.benefit.ShoppingCartPagingSource
import com.teamforce.thanksapp.domain.mappers.benefit.BenefitMapper
import com.teamforce.thanksapp.domain.mappers.benefit.ReviewMapper
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.BenefitModel
import com.teamforce.thanksapp.domain.models.benefit.ReviewModel
import com.teamforce.thanksapp.domain.repositories.BenefitRepository
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject

class BenefitRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val benefitMapper: BenefitMapper,
    private val reviewMapper: ReviewMapper
) : BenefitRepository {

    override fun getOffers(
        idCategory: Int,
        marketId: Int,
        containString: String?, minPrice: String?, maxPrice: String?
    ): Flow<PagingData<BenefitModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 5,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                BenefitPagingSource(
                    api = thanksApi,
                    benefitMapper = benefitMapper,
                    idCategory = idCategory,
                    marketId = marketId,
                    containString = containString,
                    minPrice = minPrice,
                    maxPrice = maxPrice
                )
            }
        ).flow
    }

    override suspend fun getAvailableMarkets(): ResultWrapper<List<AvailableMarketEntity>> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.getAvailableMarkets()
        }
    }

    override suspend fun getOffersById(offerId: Int, marketId: Int): ResultWrapper<BenefitItemByIdModel> {
        val result = safeApiCall(Dispatchers.IO) {
            benefitMapper.mapEntityByIdToModel(
                thanksApi.getOfferById(offerId = offerId, marketId = marketId)
            )
        }
        return result
    }

    override suspend fun loadBenefitCategories(marketId: Int): ResultWrapper<List<Category>> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getBenefitCategories(marketId = marketId)
        }
    }

    override suspend fun addBenefitToCart(
        offerId: Int,
        marketId: Int,
        quantity: Int,
        position: Int,
        isCheckedStatus: String?
    ): ResultWrapper<AddBenefitToCartResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.addBenefitToCart(
                marketId = marketId,
                data = AddBenefitToCartRequest(
                    offerId = offerId,
                    quantity = quantity,
                    isCheckedStatus
                )
            ).copy(position = position)
        }
    }

    override fun loadOffersInCart(marketId: Int): Flow<PagingData<OfferInCart>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 5,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ShoppingCartPagingSource(
                    api = thanksApi,
                    marketId = marketId
                )
            }
        ).flow
    }

    override suspend fun loadTotalPriceInCart(marketId: Int): ResultWrapper<Int> {
        var totalPrice = 0
        return safeApiCall(Dispatchers.IO){
            thanksApi.getOffersInCart(
                limit = 30,
                offset = 1,
                marketId = marketId
            ).map {
                if(it.isChecked){
                    totalPrice += it.total
                }
            }
            totalPrice
        }
    }

    override suspend fun transactionOffersFromCartToOrder(marketId: Int): ResultWrapper<TransactionOffersFromCartToOrderResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.transactionOffersFromCartToOrder(
                marketId = marketId,
                data = TransactionOffersFromCartToOrderRequest(
                    order_status = 1
                )
            )
        }
    }

    override suspend fun deleteOfferInCart(orderId: Int, marketId: Int): ResultWrapper<CancelTransactionResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.deleteOffersInCart(orderId = orderId, marketId = marketId)
        }
    }

    override fun getOrdersInHistory(listOfStatus: List<String>?, marketId: Int): Flow<PagingData<Order>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 5,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                HistoryOfOrdersPagingSource(
                    api = thanksApi,
                    listOfStatus = listOfStatus,
                    marketId = marketId
                )
            }
        ).flow
    }

    override fun getReviews(
        marketId: Int,
        offerId: Long,
        orderBy: String?,
        reverseOutput: Boolean
    ): Flow<PagingData<ReviewModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 5,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ReviewsPagingSource(
                    api = thanksApi,
                    reviewsMapper = reviewMapper,
                    offerId = offerId,
                    marketId = marketId,
                    orderBy = orderBy,
                    reverseOutput = reverseOutput
                )
            }
        ).flow
    }

    override suspend fun sendReview(
        marketId: Int,
        offerId: Long,
        imagesFilePart: List<MultipartBody.Part?>?,
        text: String,
        rate: Int
    ): ResultWrapper<CommonStatusResponse> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.createReview(
                marketId = marketId,
                offerId = offerId,
                photos = imagesFilePart,
                text = text,
                rate = rate
            )
        }
    }

    override suspend fun deleteReview(
        marketId: Int,
        reviewId: Long
    ): ResultWrapper<CommonStatusResponse> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.deleteReview(
                marketId, reviewId
            ) ?: CommonStatusResponse(-1, "")
        }
    }

    override suspend fun updateReview(
        marketId: Int,
        reviewId: Long,
        imagesFilePart: List<MultipartBody.Part?>?,
        fileList: List<ImageFileData>?,
        text: String,
        rate: Int
    ): ResultWrapper<CommonStatusResponse> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.updateReview(
                marketId = marketId,
                reviewId = reviewId,
                photos = imagesFilePart,
                fileList = fileList?.let { mapOf("fileList" to it) },
                text = text,
                rate = rate
            )
        }
    }

}