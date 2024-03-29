package com.teamforce.thanksapp.data.response.profile

import com.teamforce.thanksapp.data.entities.profile.ProfileSettingsForRequestEntity

data class ProfileSettingsResponse(
    val status: Int,
    val details: ProfileSettingsForRequestEntity
)
