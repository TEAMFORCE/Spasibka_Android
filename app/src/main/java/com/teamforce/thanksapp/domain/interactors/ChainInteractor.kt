package com.teamforce.thanksapp.domain.interactors

import androidx.paging.PagingData
import com.teamforce.thanksapp.data.sources.challenge.ChainState
import com.teamforce.thanksapp.domain.mappers.challenges.ChainMapper
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChainModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChallengeChainsModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ParticipantChainModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.StepModel
import com.teamforce.thanksapp.domain.repositories.ChallengeRepository
import com.teamforce.thanksapp.model.domain.ChallengeModel
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.mapWrapperData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChainInteractor @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val chainMapper: ChainMapper,
) {

    suspend fun loadSteps(chainId: Long): ResultWrapper<List<StepModel>>{
        return challengeRepository.loadSteps(chainId).mapWrapperData { it }
    }

    fun loadChains(chainState: ChainState? = null): Flow<PagingData<ChallengeChainsModel>>{
        return challengeRepository.loadChallengeChains(chainState)
    }

    suspend fun loadChainsOnlyFirstPage(): ResultWrapper<List<ChallengeChainsModel>>{
        return challengeRepository.loadChainsOnlyFirstPage()
    }

    suspend fun loadChain(chainId: Long): ResultWrapper<ChainModel>{
        return  challengeRepository.loadChain(chainId).mapWrapperData { chainMapper.mapChainItemToModel(it) }
    }

    fun loadChallengesFromTheChain(chainId: Long): Flow<PagingData<ChallengeModel>>{
        return challengeRepository.loadChallengesFromTheChain(chainId)
    }

    fun loadChainParticipants(chainId: Long): Flow<PagingData<ParticipantChainModel>>{
        return challengeRepository.loadChainParticipant(chainId)
    }
}