package com.teamforce.thanksapp.domain.interactors.reviews

import androidx.paging.PagingData
import com.teamforce.thanksapp.data.api.ImageFileData
import com.teamforce.thanksapp.data.response.commons.CommonStatusResponse
import com.teamforce.thanksapp.domain.mappers.benefit.ReviewMapper
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.RatingModel
import com.teamforce.thanksapp.domain.models.benefit.ReviewModel
import com.teamforce.thanksapp.domain.repositories.BenefitRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.mapWrapperData
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject

class ReviewsInteractor @Inject constructor(
    private val benefitRepository: BenefitRepository,
    private val reviewMapper: ReviewMapper
) {

    // TODO Возможно стоит убрать маппер
    fun getReviews(
        marketId: Int,
        offerId: Long,
        sortOption: ReviewsSortOption
    ): Flow<PagingData<ReviewModel>> {
//        val sortOptionInner = mapSortOption(sortOption)
        return benefitRepository.getReviews(
            marketId,
            offerId,
            sortOption.orderBy,
            sortOption.reverseOutput
        )
    }

//    private fun mapSortOption(sortOption: ReviewsSortOption): Pair<String, Boolean> {
//        return when(sortOption){
//            ReviewsSortOption.FROM_NEW_TO_OLD -> Pair("created_at", false)
//            ReviewsSortOption.FROM_OLD_TO_NEW -> Pair("created_at", true)
//            ReviewsSortOption.FROM_LOW_RATING -> Pair("rate", false)
//            ReviewsSortOption.FROM_HIGH_RATING -> Pair("rate", true)
//        }
//    }

    suspend fun sendReview(
        marketId: Int,
        offerId: Long,
        imagesFilePart: List<MultipartBody.Part?>?,
        text: String,
        rate: Int
    ): ResultWrapper<CommonStatusResponse> {
        return benefitRepository.sendReview(
            marketId, offerId, imagesFilePart, text, rate
        )
    }

    suspend fun deleteReview(
        marketId: Int,
        reviewId: Long,
    ): ResultWrapper<CommonStatusResponse> {
        return benefitRepository.deleteReview(
            marketId, reviewId
        )
    }

    suspend fun updateReview(
        marketId: Int,
        reviewId: Long,
        imagesFilePart: List<MultipartBody.Part?>?,
        fileList: List<ImageFileData>?,
        text: String,
        rate: Int
    ): ResultWrapper<CommonStatusResponse> {
        return benefitRepository.updateReview(
            marketId, reviewId, imagesFilePart, fileList, text, rate
        )
    }

    suspend fun getReview(
        marketId: Int,
        offerId: Int,
    ): ResultWrapper<RatingModel> {
        return benefitRepository.getOffersById(
            offerId, marketId
        ).mapWrapperData { reviewMapper.mapBenefitModelToRatingModel(it) }
    }
}

enum class ReviewsSortOption(val orderBy: String, val reverseOutput: Boolean) {
    FROM_NEW_TO_OLD(orderBy = "created_at", reverseOutput = false),
    FROM_OLD_TO_NEW(orderBy = "created_at", reverseOutput = true),
    FROM_LOW_RATING(orderBy = "rate", reverseOutput = false),
    FROM_HIGH_RATING(orderBy = "rate", reverseOutput = true)
}