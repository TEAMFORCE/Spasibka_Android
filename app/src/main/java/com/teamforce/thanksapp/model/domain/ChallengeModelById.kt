package com.teamforce.thanksapp.model.domain


import android.os.Parcelable
import com.teamforce.thanksapp.domain.models.challenge.ChallengeCondition
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChallengeModelById(
    val id: Int,
    val name: String?,
    val photos: List<String>?,
    val updatedAt: String?,
    val states: List<String?>?,
    val description: String,
    val creatorId: Int,
    val parameters: List<ParameterModel>?,
    val organizationId: Int?,
    val organizationName: String?,
    val fromOrganization: Boolean = false,
    val startAt: String?,
    val endAt: String?,
    val participantsTotal: Int,
    val approvedReportsAmount: Int?,
    val status: String?,
    val isNewReports: Boolean?,
    val winnersCount: Int,
    val awardees: Int,
    val creatorOrganizationId: Int,
    val fund: Int,
    val userLiked: Boolean,
    val likesAmount: Int,
    val commentsAmount: Int,
    val creatorName: String,
    val creatorSurname: String,
    val creatorPhoto: String?,
    val creatorTgName: String,
    val amICreator: Boolean,
    val active: Boolean,
    val completed: Boolean,
    val typeOfChallenge: ChallengeType = ChallengeType.DEFAULT,
    val challengeCondition: ChallengeCondition,
    val allowLikeWinners: Boolean,
    val showContenders: Boolean,
    val multipleReports: Boolean,
    val remainingTopPlaces: Int,
    val challengeWithVoting: Boolean,
    val linkToShare: String?,
    val groups: List<ChallengeGroupModel>,
    val dependencies: List<ChallengeDependencyModel>,
    val isAvailable: Boolean
) : Parcelable

@Parcelize
data class ChallengeGroupModel(
    val groupId: Long,
    val groupName: String
) : Parcelable
@Parcelize
data class ChallengeDependencyModel(
    val dependencyId: Long,
    val dependencyName: String
) : Parcelable
