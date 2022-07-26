package com.example.clouddemo

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule{

    @Provides
    fun application(): IApplication{
        return IApplication.instance()
    }
}

@HiltAndroidApp
class IApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object{
        private lateinit var application: IApplication

        @JvmStatic
        fun instance(): IApplication{
            return application
        }
    }
}