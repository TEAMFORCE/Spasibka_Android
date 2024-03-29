package com.teamforce.thanksapp

import android.app.Application
import com.teamforce.thanksapp.presentation.loaders.GlideImageLoader
import com.teamforce.thanksapp.utils.Consts
//import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import com.teamforce.photopicker.ChiliPhotoPicker
import com.teamforce.thanksapp.utils.VkSdk.VkDevUtils
import timber.log.Timber

@HiltAndroidApp
class ThanksApplication : Application() {

    override fun onCreate(){
        super.onCreate()
//        MapKitFactory.setApiKey("4ca09d6d-7461-461a-8881-5cfddd6ab428")
        VkDevUtils.initSuperAppKit(this)

        initDebug()

        ChiliPhotoPicker.init(
            loader = GlideImageLoader(),
            authority = Consts.FILE_PROVIDER
        )
    }

    private fun initDebug() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
