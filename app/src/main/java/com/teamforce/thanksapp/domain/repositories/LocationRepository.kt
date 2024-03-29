package com.teamforce.thanksapp.domain.repositories

import com.teamforce.thanksapp.data.entities.location.LocationEntity
import com.teamforce.thanksapp.utils.ResultWrapper

interface LocationRepository {
    suspend fun getLocationByIp(): ResultWrapper<LocationEntity>
}