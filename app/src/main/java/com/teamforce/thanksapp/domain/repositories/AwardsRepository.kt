package com.teamforce.thanksapp.domain.repositories

import com.teamforce.thanksapp.data.response.awards.GainAwardResponse
import com.teamforce.thanksapp.domain.models.awards.CategoryAwardsModel
import com.teamforce.thanksapp.utils.ResultWrapper

interface AwardsRepository {
    suspend fun getAwards(showOnlyMyAwards: Boolean): ResultWrapper<List<CategoryAwardsModel>>

    suspend fun getAwardsById(categoryId: Long, showOnlyMyAwards: Boolean): ResultWrapper<List<CategoryAwardsModel>>

    suspend fun gainAward(awardId: Long): ResultWrapper<GainAwardResponse>

    suspend fun setInStatusAward(awardId: Long): ResultWrapper<Any>
}