package com.teamforce.thanksapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.data.api.SectionUpdateRequest
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.entities.challenges.GetSectionsEntity
import com.teamforce.thanksapp.data.entities.challenges.challengeChains.ChainEntity
import com.teamforce.thanksapp.data.request.CheckChallengeReportRequest
import com.teamforce.thanksapp.data.response.CheckReportResponse
import com.teamforce.thanksapp.data.response.CreateReportResponse
import com.teamforce.thanksapp.data.response.GetChallengeReportDetailsResponse
import com.teamforce.thanksapp.data.response.GetChallengeResultResponse
import com.teamforce.thanksapp.data.response.GetChallengeWinnersResponse
import com.teamforce.thanksapp.data.response.commons.CommonStatusResponse
import com.teamforce.thanksapp.data.sources.challenge.ChainState
import com.teamforce.thanksapp.data.sources.challenge.ChallengeChainsPagingSource
import com.teamforce.thanksapp.data.sources.challenge.ChallengePagingSource
import com.teamforce.thanksapp.data.sources.challenge.ParticipantChainPagingSource
import com.teamforce.thanksapp.domain.mappers.challenges.ChainMapper
import com.teamforce.thanksapp.domain.mappers.challenges.ChallengeMapper
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.domain.models.challenge.ContenderModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChallengeChainsModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ParticipantChainModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.StepModel
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.CreateChallengeSettingsModel
import com.teamforce.thanksapp.domain.models.challenge.updateChallenge.UpdateChallengeModel
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.model.domain.ChallengeModel
import com.teamforce.thanksapp.model.domain.ChallengeModelById
import com.teamforce.thanksapp.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ChallengeRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val challengeMapper: ChallengeMapper,
    private val chainMapper: ChainMapper,
    private val userDataRepository: UserDataRepository,
) : ChallengeRepository {

    override suspend fun loadContenders(
        challengeId: Int,
        currenReportId: Int?,
    ): ResultWrapper<List<ContenderModel>> {
        return safeApiCall(Dispatchers.IO) {
            challengeMapper.mapContendersList(
                thanksApi.getChallengeContenders(challengeId),
                currenReportId
            )
        }
    }

    override suspend fun getSections(
        scope: ScopeRequestParams,
    ): ResultWrapper<GetSectionsEntity> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getSections(scope.id)
        }
    }

    override suspend fun removeSections(
        sectionId: Int,
    ) {
        return safeNullableApiCall(Dispatchers.IO) {
            thanksApi.removeSections(sectionId)
        }
    }

    override suspend fun updateSections(
        sectionId: Int,
        sectionName: String,
        parentSectionsIds: List<Int>,
    ): ResultWrapper<Any> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.updateSections(
                sectionId,
                SectionUpdateRequest(sectionName, parentSectionsIds)
            )
        }
    }

    override suspend fun createSections(
        sectionName: String,
        parentSectionsIds: List<Int>,
        section: ScopeRequestParams,
    ): ResultWrapper<Any> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.createSections(
                SectionUpdateRequest(
                    sectionName,
                    parentSectionsIds,
                    section.id.toLong()
                )
            )
        }
    }

    override suspend fun createReport(
        photo: MultipartBody.Part?,
        challengeId: RequestBody,
        comment: RequestBody,
    ): ResultWrapper<CreateReportResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.createChallengeReport(photo, challengeId, comment)
        }
    }

    override suspend fun checkChallengeReport(
        reportId: Int,
        state: Map<String, Char>,
        reasonOfReject: String?,
    ): ResultWrapper<CheckReportResponse> {
        val request = state["state"]?.let { CheckChallengeReportRequest(it, text = reasonOfReject) }
        return safeApiCall(Dispatchers.IO) {
            thanksApi.checkChallengeReport(reportId, request)
        }
    }

    override suspend fun loadWinners(
        challengeId: Int,
    ): ResultWrapper<List<GetChallengeWinnersResponse.Winner>> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getChallengeWinners(challengeId)
        }
    }

    override suspend fun loadChallengeResult(challengeId: Int): ResultWrapper<List<GetChallengeResultResponse>> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getChallengeResult(challengeId)
        }
    }

    override suspend fun loadChallengeWinnerReportDetails(challengeReportId: Int): ResultWrapper<GetChallengeReportDetailsResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getChallengeReportDetails(challengeReportId)
        }
    }

    override fun loadChallenge(
        activeOnly: Int,
        showDelayedChallenges: Int,
    ): Flow<PagingData<ChallengeModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ChallengePagingSource(
                    api = thanksApi,
                    activeOnly = activeOnly,
                    showDelayedChallenges = showDelayedChallenges,
                    challengeMapper = challengeMapper
                )
            }
        ).flow
    }

    override fun loadChallengeOnlyFirstPage(
        activeOnly: Int,
        showDelayedChallenges: Int,
    ): Flow<PagingData<ChallengeModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ChallengePagingSource(
                    api = thanksApi,
                    activeOnly = activeOnly,
                    showDelayedChallenges = showDelayedChallenges,
                    challengeMapper = challengeMapper,
                    onlyOnePage = true
                )
            }
        ).flow
    }

    override suspend fun deleteChallenge(challengeId: Int): ResultWrapper<CommonStatusResponse> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.deleteChallenge(challengeId)
        }
    }

    override suspend fun getChallengeById(challengeId: Int): ResultWrapper<ChallengeModelById> {
        return safeApiCall(Dispatchers.IO) {
            challengeMapper.mapChallengeEntityByIdToModelById(thanksApi.getChallenge(challengeId))
        }
    }

    override suspend fun getCreateChallengeSettings(): ResultWrapper<CreateChallengeSettingsModel> {
        return safeApiCall(Dispatchers.IO) {
            challengeMapper.mapSettingsChallenge(thanksApi.getCreateChallengeSettings())
        }
    }

    override suspend fun createChallenge(
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
    ): ResultWrapper<ChallengeModel> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.createChallenge(
                name = name,
                description = description,
                endAt = getDateInNewFormat(endAt),
                startAt = getDateInNewFormat(startAt),
                amountFund = amountFund,
                parameter_id = parameter_id,
                parameter_value = parameter_value,
                severalReports = handleBoolInStringYesNo(severalReports),
                challengeType = handleVotingChallenge(challengeType),
                showContenders = handleBoolInStringYesNo(showContenders),
                debitAccountId = debitAccountId,
                templateId = templateId,
                photos = photos
            )
        }
    }

    override suspend fun updateChallenge(
        challengeId: Int,
        challengeData: UpdateChallengeModel,
    ): ResultWrapper<CommonStatusResponse> {
        return safeApiCall(Dispatchers.IO) {
            val model = challengeMapper.mapUpdateChallengeModel(challengeData)
            thanksApi.updateChallenge(
                challengeId = challengeId,
                fileList = challengeData.fileList?.let { mapOf("fileList" to it) },
                photos = challengeData.photos,
                startAt = model.startAt,
                endAt = model.endAt,
                challengeType = model.challengeType,
                multipleReports = model.multipleReports,
                debitAccountId = model.accountId,
                showContenders = model.showContenders,
                startBalance = model.startBalance,
                winnersCount = model.winnersCount,
                name = model.name,
                description = model.description
            )
        }
    }

    private fun handleVotingChallenge(boolean: Boolean): String {
        return if (boolean) ChallengeType.VOTING.typeOfChallenge else ChallengeType.DEFAULT.typeOfChallenge
    }

    private fun getDateInNewFormat(dateTime: ZonedDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
    }

    private fun handleBoolInStringYesNo(boolean: Boolean): String {
        return if (boolean) "yes" else "no"
    }


    override fun loadChallengeChains(
        chainState: ChainState?
    ): Flow<PagingData<ChallengeChainsModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ChallengeChainsPagingSource(
                    api = thanksApi,
                    organizationId = userDataRepository.getCurrentOrg(),
                    chainMapper = chainMapper,
                    chainState = chainState
                )
            }
        ).flow
    }

    override suspend fun loadChainsOnlyFirstPage(): ResultWrapper<List<ChallengeChainsModel>> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.getChallengeChains(
                organizationId = userDataRepository.getCurrentOrg() ?: "0",
                    limit = 10,
                    offset = 1,
                    state = ChainState.ACTIVE.designation
                )
        }.mapWrapperData {
            chainMapper.mapChallengeChains(it.data)
        }
    }

    override suspend fun loadChain(chainId: Long): ResultWrapper<ChainEntity> {
        return safeApiCall(Dispatchers.IO) {
            thanksApi.getChain(
                organizationId = userDataRepository.getCurrentOrg().toString(),
                chainId = chainId.toString()
            )
        }
    }


    override fun loadChallengesFromTheChain(chainId: Long): Flow<PagingData<ChallengeModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ChallengePagingSource(
                    api = thanksApi,
                    activeOnly = 1,
                    showDelayedChallenges = 1,
                    chainId = chainId,
                    challengeMapper = challengeMapper
                )
            }
        ).flow
    }


    override suspend fun loadSteps(chainId: Long): ResultWrapper<List<StepModel>> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.getChallenges(
                chainId = chainId
            ).data
        }.mapWrapperData { challengeEntities -> chainMapper.mapChallengeListToChallengeByStep(challengeEntities) }
    }


    override fun loadDependentChallenges(challengeId: Int): Flow<PagingData<ChallengeModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ChallengePagingSource(
                    api = thanksApi,
                    activeOnly = 0,
                    showDelayedChallenges = 0,
                    challengeId = challengeId,
                    challengeMapper = challengeMapper
                )
            }
        ).flow
    }

    override fun loadChainParticipant(chainId: Long): Flow<PagingData<ParticipantChainModel>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ParticipantChainPagingSource(
                    api = thanksApi,
                    organizationId = userDataRepository.getCurrentOrg(),
                    chainMapper = chainMapper,
                    chainId = chainId
                )
            }
        ).flow
    }
}
