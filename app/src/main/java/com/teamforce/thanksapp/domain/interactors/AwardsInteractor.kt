package com.teamforce.thanksapp.domain.interactors

import com.teamforce.thanksapp.data.response.awards.GainAwardResponse
import com.teamforce.thanksapp.domain.models.awards.CategoryAwardsModel
import com.teamforce.thanksapp.domain.repositories.AwardsRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import javax.inject.Inject

class AwardsInteractor @Inject constructor(
   private val awardsRepository: AwardsRepository
) {
    suspend fun loadAwards(showOnlyMyAwards: Boolean): ResultWrapper<List<CategoryAwardsModel>>{
        return awardsRepository.getAwards(showOnlyMyAwards)
    }

    suspend fun loadAwardsById(categoryId: Long, showOnlyMyAwards: Boolean): ResultWrapper<List<CategoryAwardsModel>>{
        return awardsRepository.getAwardsById(categoryId, showOnlyMyAwards)
    }

    suspend fun gainAward(awardId: Long): ResultWrapper<GainAwardResponse>{
        return awardsRepository.gainAward(awardId)
    }

    suspend fun setInStatusAward(awardId: Long): ResultWrapper<Any>{
        return awardsRepository.setInStatusAward(awardId)
    }
}