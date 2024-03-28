package com.teamforce.thanksapp.domain.mappers.challenges

import com.teamforce.thanksapp.data.entities.challenges.*
import com.teamforce.thanksapp.data.entities.challenges.challengeChains.ChallengeChainsEntity
import com.teamforce.thanksapp.data.response.GetChallengeContendersResponse
import com.teamforce.thanksapp.domain.models.challenge.ChallengeCondition
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.domain.models.challenge.ContenderModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChainCondition
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChallengeChainsModel
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.CreateChallengeSettingsModel
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.DebitAccountModel
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.TypesChallengeModel
import com.teamforce.thanksapp.domain.models.challenge.updateChallenge.UpdateChallengeModel
import com.teamforce.thanksapp.model.domain.ChallengeDependencyModel
import com.teamforce.thanksapp.model.domain.ChallengeGroupModel
import com.teamforce.thanksapp.model.domain.ChallengeModel
import com.teamforce.thanksapp.model.domain.ChallengeModelById
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.concatenate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class ChallengeMapper @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {

    fun mapUpdateChallengeModel(from: UpdateChallengeModel): UpdateChallengeEntity {
        return UpdateChallengeEntity(
            name = from.name,
            description = from.description,
            startAt = getDateInBackEndFormat(from.startAt),
            endAt = getDateInBackEndFormat(from.endAt),
            startBalance = from.startBalance,
            winnersCount = from.winnersCount,
            accountId = from.accountId,
            multipleReports = from.multipleReports,
            challengeType = from.challengeWithVoting?.let(::handleVotingChallenge),
            showContenders = from.showContenders,
        )
    }

    private fun handleVotingChallenge(boolean: Boolean): String {
        return if (boolean) ChallengeType.VOTING.typeOfChallenge else ChallengeType.DEFAULT.typeOfChallenge
    }

    private fun getDateInBackEndFormat(dateTime: ZonedDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
    }

    fun mapContendersList(
        from: List<GetChallengeContendersResponse.Contender>,
        currentReportId: Int?,
    ): List<ContenderModel> {
        return from.map {
            mapContender(it, currentReportId)
        }
    }

    private fun mapContender(from: GetChallengeContendersResponse.Contender, currentReportId: Int?): ContenderModel {
        return ContenderModel(
            reportId = from.report_id,
            reportPhoto = from.report_photo,
            reportCreatedAt = from.report_created_at,
            reportText = from.report_text,
            participantId = from.participant_id,
            participantName = from.participant_name,
            participantSurname = from.participant_surname,
            participantPhoto = from.participant_photo,
            userLiked = from.userLiked,
            likesAmount = from.likes_amount,
            open = checkOpen(currentReportId, realReportId = from.report_id),
            canApprove = from.can_approve,
            myReport = from.my_report,
            commentsAmount = from.comments_amount
        )
    }

    private fun checkOpen(currentReportId: Int?, realReportId: Int): Boolean {
        currentReportId?.let {
            return currentReportId == realReportId
        }
        return false
    }

    fun mapChallenges(from: List<ChallengeEntity>): List<ChallengeModel> {
        return from.map { mapChallengeEntityToModel(it) }
    }

    fun mapChallengeEntityByIdToModelById(from: ChallengeEntityById): ChallengeModelById {
        return ChallengeModelById(
            id = from.id,
            name = from.name,
            photos = mapPhoto(from.photo, from.photos),
            updatedAt = from.updatedAt,
            states = from.states,
            description = from.description,
            creatorId = from.creatorId,
            parameters = from.parameters,
            endAt = from.endAt,
            approvedReportsAmount = from.approvedReportsAmount,
            status = from.status,
            isNewReports = from.isNewReports,
            winnersCount = from.winnersCount,
            awardees = from.awardees,
            creatorOrganizationId = from.creatorOrganizationId,
            fund = from.fund,
            likesAmount = from.likesAmount,
            creatorName = from.creatorName,
            creatorSurname = from.creatorSurname,
            creatorPhoto = from.creatorPhoto,
            creatorTgName = from.creatorTgName,
            active = from.active,
            completed = from.completed,
            typeOfChallenge = mapAlgorithmType(from.algorithmType),
            userLiked = from.userLiked,
            challengeCondition = mapChallengeCondition(from.challengeCondition),
            allowLikeWinners = from.allowLikeWinners,
            multipleReports = from.multipleReports,
            remainingTopPlaces = from.remainingTopPlaces,
            showContenders = from.showContenders,
            fromOrganization = from.fromOrganization,
            organizationId = from.organizationId,
            organizationName = from.organizationName,
            startAt = from.startAt,
            amICreator = from.creatorId == userDataRepository.getProfileId()?.toInt(),
            participantsTotal = from.participantsTotal,
            challengeWithVoting = checkIsVotingChallenge(from.algorithmName),
            linkToShare = from.linkToShare,
            groups = mapGroups(from.groups),
            dependencies = mapDependencies(from.dependencies),
            isAvailable = from.isAvailable,
            commentsAmount = from.commentsAmount
        )
    }

    private fun mapGroups(from: List<ChallengeGroupEntity>): List<ChallengeGroupModel>{
        return from.map {
            ChallengeGroupModel(
                groupId = it.groupId,
                groupName = it.groupName
            )
        }
    }

    private fun mapDependencies(from: List<ChallengeDependencyEntity>): List<ChallengeDependencyModel>{
        return from.map {
            ChallengeDependencyModel(
                dependencyId = it.dependencyId,
                dependencyName = it.dependencyName
            )
        }
    }

    private fun checkIsVotingChallenge(algorithmName: String?): Boolean {
        return try {
            val type = ChallengeType.valueOf(algorithmName?.uppercase(Locale.ROOT) ?: ChallengeType.DEFAULT.name)
            type == ChallengeType.VOTING
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun mapAlgorithmType(algorithmType: Int): ChallengeType {
        return if(algorithmType == 1){
            ChallengeType.DEFAULT
        }else{
            ChallengeType.VOTING
        }
    }

     fun mapPhoto(photo: String?, photos: List<String?>?): List<String>? {
        return photos?.filterNotNull() ?: if (photo != null) listOf(photo)
        else null
    }

    private fun mapChallengeEntityToModel(from: ChallengeEntity): ChallengeModel {
        return ChallengeModel(
            id = from.id,
            name = from.name,
            photo = mapPhoto(from.photo, from.photos),
            updatedAt = from.updatedAt,
            states = from.states,
            startBalance = from.startBalance,
            creatorId = from.creatorId,
            creatorName = from.creatorName,
            creatorSurname = from.creatorSurname,
            winnersCount = from.winnersCount,
            parameters = from.parameters,
            challengeCondition = mapChallengeCondition(from.challengeCondition),
            approvedReportsAmount = from.approvedReportsAmount,
            fund = from.fund,
            status = from.status,
            isNewReports = from.isNewReports,
            prizeSize = from.prizeSize,
            awardees = from.awardees,
            active = from.active,
            fromOrganization = from.fromOrganization,
            organizationName = from.organizationName,
        )
    }

    fun mapChallengeCondition(challengeCondition: String): ChallengeCondition {
        return when (challengeCondition) {
            "A" -> ChallengeCondition.ACTIVE
            "W" -> ChallengeCondition.DEFERRED
            "F" -> ChallengeCondition.FINISHED
            else -> ChallengeCondition.FINISHED
        }
    }

    fun mapSettingsChallenge(from: CreateChallengeSettingsEntity): CreateChallengeSettingsModel {
        return CreateChallengeSettingsModel(
            accounts = mapListAccount(from.accounts),
            multipleReports = handleYesNoToBool(from.multiple_reports),
            showContenders = handleYesNoToBool(from.show_contenders),
            challengeWithVoting = false
        )
    }

    private fun handleYesNoToBool(string: String): Boolean {
        return when (string) {
            "yes" -> true
            else -> false
        }
    }

    private fun mapListAccount(from: AccountsEntity): List<DebitAccountModel> {
        val commonList = concatenate(from.personal_accounts, from.organization_accounts)
        val debitsAccount = commonList.map { debitAccount -> mapDebitAccount(debitAccount) }

        val firstMyAccountIndex = debitsAccount.indexOfFirst { it.myAccount }

        // Установить current = true только для первого попавшегося элемента с myAccount = true
        debitsAccount.forEachIndexed { index, debitAccountModel ->
            debitAccountModel.current = index == firstMyAccountIndex
        }

        return debitsAccount
    }

    private fun mapDebitAccount(from: DebitAccountEntity): DebitAccountModel {
        return DebitAccountModel(
            id = from.id,
            ownerId = from.owner_id,
            amount = from.amount,
            accountType = from.account_type,
            organizationName = from.organization_name,
            myAccount = from.owner_id == userDataRepository.getProfileId()?.toInt(),
        )
    }
}