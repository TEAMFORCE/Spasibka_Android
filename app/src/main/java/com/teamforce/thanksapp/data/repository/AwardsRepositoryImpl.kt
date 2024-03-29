package com.teamforce.thanksapp.data.repository

import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.request.awards.GainAwardRequest
import com.teamforce.thanksapp.data.request.awards.SetInStatusAwardRequest
import com.teamforce.thanksapp.data.response.awards.AwardEntity
import com.teamforce.thanksapp.data.response.awards.GainAwardResponse
import com.teamforce.thanksapp.domain.mappers.awards.AwardsMapper
import com.teamforce.thanksapp.domain.models.awards.AwardState
import com.teamforce.thanksapp.domain.models.awards.CategoryAwardsModel
import com.teamforce.thanksapp.domain.repositories.AwardsRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.mapWrapperData
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class AwardsRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val mapper: AwardsMapper,
    private val userDataRepository: UserDataRepository,
) : AwardsRepository {

    var awards: List<CategoryAwardsModel> = listOf()
    var myAwards: List<CategoryAwardsModel> = listOf()


    override suspend fun getAwards(showOnlyMyAwards: Boolean): ResultWrapper<List<CategoryAwardsModel>> {
        return safeApiCall(Dispatchers.IO) {
            val orgId = userDataRepository.getCurrentOrg()?.toInt()
            if (showOnlyMyAwards) thanksApi.getAwards(orgId, true).data
            else thanksApi.getAwards(orgId).data
        }.mapWrapperData {
            if (showOnlyMyAwards) {
                myAwards = mapper.mapAwardsListToCategoryList(it)
                myAwards
            } else {
                awards = mapper.mapAwardsListToCategoryList(it)
                awards
            }

        }

    }

    override suspend fun getAwardsById(
        categoryId: Long,
        showOnlyMyAwards: Boolean
    ): ResultWrapper<List<CategoryAwardsModel>> {
        return safeApiCall(Dispatchers.IO) {
            if (showOnlyMyAwards) {
                myAwards.filter { it.id == categoryId }
            } else awards.filter { it.id == categoryId }
        }.mapWrapperData { it }
    }

    override suspend fun gainAward(awardId: Long): ResultWrapper<GainAwardResponse> {
        return safeApiCall(Dispatchers.IO) {
            val orgId = userDataRepository.getCurrentOrg()?.toInt()
            val userId = userDataRepository.getProfileId()?.toLong()
            thanksApi.gainAward(orgId = orgId,
                data = GainAwardRequest(
                    userId = userId,
                    awardId = awardId
                )
            )
        }.mapWrapperData { it }
    }

    override suspend fun setInStatusAward(awardId: Long): ResultWrapper<Any> {
        return safeApiCall(Dispatchers.IO){
            val orgId = userDataRepository.getCurrentOrg()?.toInt()
            thanksApi.setInStatusAward(orgId = orgId,
                data = SetInStatusAwardRequest(
                    awardId = awardId
                )
            )
        }.mapWrapperData { it }
    }

}