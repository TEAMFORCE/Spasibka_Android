package com.teamforce.thanksapp.data.repository

import com.teamforce.thanksapp.data.api.LocationApi
import com.teamforce.thanksapp.data.entities.location.LocationEntity
import com.teamforce.thanksapp.domain.repositories.LocationRepository
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationApi: LocationApi
): LocationRepository  {
    override suspend fun getLocationByIp(): ResultWrapper<LocationEntity> {
        return safeApiCall(Dispatchers.IO) {
            locationApi.getLocationByIp()
        }
    }
}