package com.teamforce.thanksapp.domain.mappers.templates

import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.data.entities.templates.TemplateEntity
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.domain.models.templates.TemplateModel
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryMapper
import com.teamforce.thanksapp.utils.UserDataRepository
import javax.inject.Inject

class TemplatesMapper @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val categoryMapper: CategoryMapper,
) {

    fun mapTemplates(from: List<TemplateEntity>, scopeOfTemplates: ScopeRequestParams): List<TemplateModel> {
        return from.map { mapTemplateEntityToModel(it, scopeOfTemplates) }
    }

    private fun mapTemplateEntityToModel(from: TemplateEntity, scopeOfTemplates: ScopeRequestParams): TemplateModel {
        return TemplateModel(
            id = from.id,
            challengeType = mapChallengeType(from.challenge_type),
            description = from.description,
            name = from.name,
            showContenders = from.show_contenders,
            multipleReports = from.multiple_reports,
            parameters = from.parameters,
            photos = from.photos?.mapNotNull { it } ?: listOf(),
            sections = categoryMapper.mapItems(from.sections),
            status = from.status,
            editableByMe = canIEditThisTemplate(scopeOfTemplates, userDataRepository.userIsSuperUser(), userDataRepository.userIsAdmin()),
            scope = scopeOfTemplates,
        )
    }

    private fun canIEditThisTemplate(scopeOfTemplates: ScopeRequestParams, amISuperUser: Boolean, amIAdmin: Boolean): Boolean{
        return when(scopeOfTemplates){
            ScopeRequestParams.TEMPLATES -> true
            ScopeRequestParams.ORGANIZATION -> return amISuperUser || amIAdmin
            ScopeRequestParams.COMMON -> return amISuperUser
        }
    }

    private fun mapChallengeType(type: String): Boolean {
        return when(type){
            ChallengeType.VOTING.typeOfChallenge -> true
            else -> false
        }
    }
}