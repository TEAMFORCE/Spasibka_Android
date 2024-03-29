package com.teamforce.thanksapp.di

import android.content.Context
import android.content.SharedPreferences
import com.teamforce.thanksapp.utils.Consts
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedPreferencesModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Consts.APPLICATION_ID, Context.MODE_PRIVATE)
    }

}