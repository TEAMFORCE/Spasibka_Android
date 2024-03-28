package com.teamforce.thanksapp.domain.repositories

import com.teamforce.thanksapp.data.entities.branding.OrganizationBrandingEntity
import com.teamforce.thanksapp.domain.models.branding.OrganizationBrandingModel
import com.teamforce.thanksapp.utils.ResultWrapper

interface BrandingRepository {

     fun getLocalOrganizationBranding(): OrganizationBrandingModel

    suspend fun getRemoteOrganizationBranding(organizationId: Int): ResultWrapper<OrganizationBrandingModel>

}