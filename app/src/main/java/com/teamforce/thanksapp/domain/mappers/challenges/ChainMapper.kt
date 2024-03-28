package com.teamforce.thanksapp.domain.mappers.challenges

import com.teamforce.thanksapp.data.entities.challenges.ChallengeEntity
import com.teamforce.thanksapp.data.entities.challenges.challengeChains.ChainEntity
import com.teamforce.thanksapp.data.entities.challenges.challengeChains.ChallengeChainsEntity
import com.teamforce.thanksapp.data.entities.challenges.challengeChains.ParticipantChainEntity
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.*
import javax.inject.Inject

class ChainMapper @Inject constructor(
    private val challengeMapper: ChallengeMapper
) {

    fun mapChallengeListToChallengeByStep(from: List<ChallengeEntity>): List<StepModel> {
        if(from.isNotEmpty()){
            val groupedChallenges = from.groupBy { it.step }

            val stepModels = groupedChallenges.values.map { challenges ->
                StepModel(
                    step = challenges.first().step,
                    tasks = challenges.toTask(),
                    isOpen = challenges.first().step == 0,
                    status = StepModel.StepStatus.COMPLETED,
                    info_msg = "message",
                )
            }
            val result = stepModels.sortedBy { it.step }
            result.first().isOpen = true
            return result
        }
        return emptyList()
    }

    private fun List<ChallengeEntity>.toTask(): List<StepModel.Task>{
        return this.map {
            it.toTask()
        }
    }

    private fun ChallengeEntity.toTask(): StepModel.Task{
        return StepModel.Task(
            id = this.id.toLong(),
            name = name ?: "",
            creatorName = "$creatorName $creatorSurname",
            reward = this.prizeSize,
            image = challengeMapper.mapPhoto(this.photo, this.photos)?.first(),
            status = challengeMapper.mapChallengeCondition(this.challengeCondition)
        )
    }

//    private fun challengeEntityToStepModel(from: List<ChallengeChainsEntity>)

    fun mapChallengeChains(from: List<ChallengeChainsEntity>): List<ChallengeChainsModel> {
        return from.map { mapChallengeChainEntityToModel(it) }
    }

    private fun mapChallengeChainEntityToModel(from: ChallengeChainsEntity): ChallengeChainsModel {
        return ChallengeChainsModel(
            id = from.id,
            name = from.name,
            author = from.author,
            authorId = from.authorId,
            authorPhoto = from.authorPhoto,
            photos = from.photos?.filterNotNull() ?: listOf(),
            updatedAt = from.updatedAt,
            contendersTotal = from.contendersTotal,
            commentsTotal = from.commentsTotal,
            taskTotal = from.taskTotal,
            tasksFinished = from.tasksFinished,
            currentState = mapChainCondition(from.currentState)
        )
    }

    private fun mapChainCondition(chainCondition: String): ChainCondition {
        return when (chainCondition) {
            "A" -> ChainCondition.ACTIVE
            "D" -> ChainCondition.DEFERRED
            "F" -> ChainCondition.COMPLETED
            else -> ChainCondition.COMPLETED
        }
    }

    fun mapChainItemToModel(from: ChainEntity): ChainModel{
        return ChainModel(
            id = from.id,
            name = from.name,
            description = from.description,
            author = from.author,
            authorId = from.authorId,
            authorPhoto = from.authorPhoto,
            photos = from.photos?.filterNotNull() ?: listOf(),
            updatedAt = from.updatedAt,
            startAt = from.startAt,
            endAt = from.endAt,
            contendersTotal = from.contendersTotal,
            commentsTotal = from.commentsTotal,
            taskTotal = from.taskTotal,
            tasksFinished = from.tasksFinished,
            currentState = mapChainCondition(from.currentState)
        )
    }

    fun mapListParticipants(from: List<ParticipantChainEntity>): List<ParticipantChainModel>{
        return from.map { it.toModel() }
    }

    private fun ParticipantChainEntity.toModel(): ParticipantChainModel{
        return ParticipantChainModel(
            id = this.participantId,
            name = this.participantName,
            photo = this.participantPhoto
        )
    }
}