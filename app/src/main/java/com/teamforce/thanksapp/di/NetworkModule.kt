package com.teamforce.thanksapp.di


import com.teamforce.thanksapp.data.SharedPreferencesWrapperUserData
import com.teamforce.thanksapp.data.api.LocationApi
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.utils.Consts
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    @mainApi
    fun provideOkHttpClient(authTokenInterceptor: AuthTokenInterceptor): OkHttpClient {
        return OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(authTokenInterceptor)
            .build()
    }


    @Provides
    @Singleton
    @mainApi
    fun provideRetrofit(@mainApi client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    fun provideThanksApi(@mainApi retrofit: Retrofit): ThanksApi {
        return retrofit.create(ThanksApi::class.java)
    }

    @Provides
    @Singleton
    @locationApi
    fun provideLocationOkHttpClient() : OkHttpClient {
        return OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    @Provides
    @Singleton
    @locationApi
    fun provideLocationRetrofit(@locationApi client: OkHttpClient): Retrofit {
        return  Retrofit.Builder()
            .baseUrl("http://ip-api.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    fun provideLocationApi(@locationApi retrofit: Retrofit): LocationApi {
        return  retrofit.create(LocationApi::class.java)
    }
}

class AuthTokenInterceptor
@Inject constructor(private val sharedPreferences: SharedPreferencesWrapperUserData) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request()
            .newBuilder()

        if (sharedPreferences.getToken() != null) builder.addHeader(
            "Authorization",
            "Token ${sharedPreferences.getToken()!!}"
        )
        // Fix Protocol Exception Unexpected Status Line при перелогине
        builder.addHeader("Connection", "close")
        // ТАк же можно только к 1 запросу дописать такое в ThanksApi @Headers("Connection:close"), будет тоже самое

        return chain.proceed(builder.build())
    }

}

@Qualifier
annotation class mainApi

@Qualifier
annotation class locationApi