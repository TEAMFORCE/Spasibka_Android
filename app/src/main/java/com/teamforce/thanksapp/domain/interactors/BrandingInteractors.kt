package com.teamforce.thanksapp.domain.interactors

import com.teamforce.thanksapp.domain.models.branding.OrganizationBrandingModel
import com.teamforce.thanksapp.domain.repositories.BrandingRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import javax.inject.Inject

class BrandingInteractors @Inject constructor(
    private val brandingRepository: BrandingRepository
) {
    suspend fun getRemoteOrganizationBrand(organizationId: Int): ResultWrapper<OrganizationBrandingModel>{
        return brandingRepository.getRemoteOrganizationBranding(organizationId)
    }

     fun getLocalOrganizationBrand(): OrganizationBrandingModel{
        return brandingRepository.getLocalOrganizationBranding()
    }
}