package com.teamforce.thanksapp.data.repository

import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.domain.mappers.branding.BrandingMapper
import com.teamforce.thanksapp.domain.models.branding.OrganizationBrandingModel
import com.teamforce.thanksapp.domain.repositories.BrandingRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.branding.BrandingSharedPref
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class BrandingRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val brandingMapper: BrandingMapper,
    private val brandingSharedPref: BrandingSharedPref
) : BrandingRepository {
    override fun getLocalOrganizationBranding(): OrganizationBrandingModel {
        val response = brandingSharedPref.getBrandingTheme()
        setDataBranding(response)
        return response
    }

    override suspend fun getRemoteOrganizationBranding(organizationId: Int): ResultWrapper<OrganizationBrandingModel> {
        return safeApiCall(Dispatchers.IO) {
            val mappedResponse = brandingMapper.mapBrandingEntityToModel(
                thanksApi.getOrganizationBranding(organizationId),
                oldColor = Branding.appTheme
            )
            brandingSharedPref.safeBrandingTheme(mappedResponse)
            setDataBranding(mappedResponse)
            return@safeApiCall mappedResponse
        }
    }
}

private fun setDataBranding(brand: OrganizationBrandingModel) {
    Branding.forms = brand.bonusName.RU
    Branding.appTheme = brand.colorsJson
    Branding.brand = brand
}
