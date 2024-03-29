package com.teamforce.thanksapp.domain.repositories

import androidx.paging.PagingData
import com.teamforce.thanksapp.domain.models.challenge.ContenderModel
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.data.entities.challenges.GetSectionsEntity
import com.teamforce.thanksapp.data.entities.challenges.challengeChains.ChainEntity
import com.teamforce.thanksapp.data.response.CheckReportResponse
import com.teamforce.thanksapp.data.response.CreateReportResponse
import com.teamforce.thanksapp.data.response.GetChallengeReportDetailsResponse
import com.teamforce.thanksapp.data.response.GetChallengeResultResponse
import com.teamforce.thanksapp.data.response.GetChallengeWinnersResponse
import com.teamforce.thanksapp.data.response.commons.CommonStatusResponse
import com.teamforce.thanksapp.data.sources.challenge.ChainState
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChallengeChainsModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ParticipantChainModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.StepModel
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.CreateChallengeSettingsModel
import com.teamforce.thanksapp.domain.models.challenge.updateChallenge.UpdateChallengeModel
import com.teamforce.thanksapp.model.domain.ChallengeModel
import com.teamforce.thanksapp.model.domain.ChallengeModelById
import com.teamforce.thanksapp.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.time.ZonedDateTime

interface ChallengeRepository {

    suspend fun loadSteps(chainId: Long): ResultWrapper<List<StepModel>>

     suspend fun deleteChallenge(challengeId: Int): ResultWrapper<CommonStatusResponse>

    suspend fun loadContenders(challengeId: Int, currentReportId: Int?): ResultWrapper<List<ContenderModel>>

    suspend fun createReport(
        photo: MultipartBody.Part?,
        challengeId: RequestBody,
        comment: RequestBody
    ): ResultWrapper<CreateReportResponse>

    suspend fun checkChallengeReport(
        reportId: Int,
        state: Map<String, Char>,
        reasonOfReject: String?
    ): ResultWrapper<CheckReportResponse>

    suspend fun loadWinners(
        challengeId: Int,
    ): ResultWrapper<List<GetChallengeWinnersResponse.Winner>>

    suspend fun loadChallengeResult(
        challengeId: Int,
    ): ResultWrapper<List<GetChallengeResultResponse>>

    suspend fun loadChallengeWinnerReportDetails(
        challengeReportId: Int,
    ): ResultWrapper<GetChallengeReportDetailsResponse>

    fun loadChallenge(
        activeOnly: Int = 0,
        showDelayedChallenges: Int = 0
    ): Flow<PagingData<ChallengeModel>>

    fun loadChallengeOnlyFirstPage(
        activeOnly: Int = 0,
        showDelayedChallenges: Int = 0
    ): Flow<PagingData<ChallengeModel>>

    fun loadChallengeChains(chainState: ChainState? = null): Flow<PagingData<ChallengeChainsModel>>

    suspend fun loadChainsOnlyFirstPage(): ResultWrapper<List<ChallengeChainsModel>>

    suspend fun loadChain(chainId: Long): ResultWrapper<ChainEntity>

    suspend fun getChallengeById(challengeId: Int): ResultWrapper<ChallengeModelById>

    suspend fun getCreateChallengeSettings(): ResultWrapper<CreateChallengeSettingsModel>

    suspend fun createChallenge(
        name: String,
        description: String,
        endAt: ZonedDateTime?,
        startAt: ZonedDateTime?,
        amountFund: Int,
        parameter_id: Int,
        parameter_value: Int,
        challengeType: Boolean,
        severalReports: Boolean,
        showContenders: Boolean,
        debitAccountId: Int?,
        templateId: Int?,
        photos: List<MultipartBody.Part>,
    ): ResultWrapper<ChallengeModel>

    suspend fun updateChallenge(
       challengeId: Int,
       challengeData: UpdateChallengeModel
    ): ResultWrapper<CommonStatusResponse>

    /**
    0 - my sections
    1 - organization sections (by default)
    2 - common sections
     */
    suspend fun getSections(scope: ScopeRequestParams = ScopeRequestParams.COMMON): ResultWrapper<GetSectionsEntity>
    suspend fun removeSections(sectionId: Int)
    suspend fun updateSections(sectionId: Int, sectionName: String, parentSectionsIds: List<Int>): ResultWrapper<Any>
    suspend fun createSections(sectionName: String, parentSectionsIds: List<Int>, section: ScopeRequestParams): ResultWrapper<Any>

    fun loadChallengesFromTheChain(
        chainId: Long,
    ): Flow<PagingData<ChallengeModel>>

    fun loadDependentChallenges(
        challengeId: Int,
    ): Flow<PagingData<ChallengeModel>>

    fun loadChainParticipant(
        chainId: Long,
    ): Flow<PagingData<ParticipantChainModel>>
}
