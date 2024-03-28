package com.teamforce.thanksapp.data.api

import com.teamforce.thanksapp.data.entities.location.LocationEntity
import retrofit2.http.GET

interface LocationApi {
    @GET("/json/?fields=city,lat,lon,offset&lang=ru")
    suspend fun getLocationByIp() : LocationEntity
}