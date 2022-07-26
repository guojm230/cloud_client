package com.example.repository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
object HiltModule{

    @Provides
    @Singleton
    fun httpClient(): OkHttpClient{
        return OkHttpClient().apply {

        }
    }

}