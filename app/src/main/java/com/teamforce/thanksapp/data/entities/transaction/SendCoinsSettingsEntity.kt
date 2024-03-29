package com.teamforce.thanksapp.data.entities.transaction

import com.google.gson.annotations.SerializedName
import com.teamforce.thanksapp.model.domain.TagModel

data class SendCoinsSettingsEntity(
    @SerializedName("is_public")
    val isPublic: Boolean,
    @SerializedName("is_anonymous_available")
    val isAnonymousAvailable: Boolean,
    val tags: List<TagModel>
)