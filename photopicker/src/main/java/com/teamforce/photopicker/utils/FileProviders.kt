package com.teamforce.photopicker.utils

import android.content.Context
import androidx.core.content.FileProvider
import com.teamforce.photopicker.PickerConfiguration
import java.io.File

internal fun File.providerUri(context: Context) =
    FileProvider.getUriForFile(context, PickerConfiguration.getAuthority(), this)
