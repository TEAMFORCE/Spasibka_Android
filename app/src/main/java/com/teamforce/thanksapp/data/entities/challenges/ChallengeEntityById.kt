package com.teamforce.thanksapp.data.entities.challenges

import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.model.domain.ParameterModel

data class ChallengeEntityById(
    val id: Int,
    val name: String?,
    val photo: String?,
    val photos: List<String?>,
    @SerializedName("updated_at")
    val updatedAt: String?,
    val states: List<String?>?,
    val description: String,
    @SerializedName("creator_id")
    val creatorId: Int,
    val parameters: List<ParameterModel>?,
    @SerializedName("participants_total")
    val participantsTotal: Int,
    @SerializedName("organization_id")
    val organizationId: Int?,
    @SerializedName("organization_name")
    val organizationName: String?,
    @SerializedName("from_organization")
    val fromOrganization: Boolean = false,
    @SerializedName("start_at")
    val startAt: String?,
    @SerializedName("end_at")
    val endAt: String?,
    @SerializedName("approved_reports_amount")
    val approvedReportsAmount: Int?,
    val status: String?,
    @SerializedName("is_new_reports")
    val isNewReports: Boolean?,
    @SerializedName("winners_count")
    val winnersCount: Int,
    val awardees: Int,
    @SerializedName("creator_organization_id")
    val creatorOrganizationId: Int,
    val fund: Int,
    @SerializedName("user_liked")
    val userLiked: Boolean,
    @SerializedName("likes_amount")
    val likesAmount: Int,
    @SerializedName("comments_amount")
    val commentsAmount: Int,
    @SerializedName("creator_name")
    val creatorName: String,
    @SerializedName("creator_surname")
    val creatorSurname: String,
    @SerializedName("creator_photo")
    val creatorPhoto: String?,
    @SerializedName("creator_tg_name")
    val creatorTgName: String,
    val active: Boolean,
    val completed: Boolean,
    @SerializedName("algorithm_name")
    val algorithmName: String?,
    @SerializedName("algorithm_type")
    val algorithmType: Int,
    @SerializedName("challenge_condition")
    val challengeCondition: String,
    @SerializedName("allow_like_winners")
    val allowLikeWinners: Boolean,
    @SerializedName("show_contenders")
    val showContenders: Boolean,
    @SerializedName("multiple_reports")
    val multipleReports: Boolean,
    @SerializedName("remaining_top_places")
    val remainingTopPlaces: Int,
    @SerializedName("link_to_share")
    val linkToShare: String?,
    val groups: List<ChallengeGroupEntity>,
    val dependencies: List<ChallengeDependencyEntity>,
    @SerializedName("is_available") val isAvailable: Boolean
)

data class ChallengeGroupEntity(
    @SerializedName("group_id")
    val groupId: Long,
    @SerializedName("group_name")
    val groupName: String
)

data class ChallengeDependencyEntity(
    @SerializedName("dependency_id")
    val dependencyId: Long,
    @SerializedName("dependency_name")
    val dependencyName: String
)

/*
organization_id (INT) - айди организации
                    organization_name (STR) - название организации
                    from_organization (BOOL) - проводится ли челлендж от организации (награды раздаются со счёта организации)
 */
