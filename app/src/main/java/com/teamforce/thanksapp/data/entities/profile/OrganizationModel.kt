package com.teamforce.thanksapp.data.entities.profile

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrganizationModel(
    val id: Int,
    val name: String,
    val organization_photo: String?,
    val is_current: Boolean
) : Parcelable
