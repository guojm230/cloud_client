package com.example.clouddemo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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
        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = getString(R.string.notification_channel_name)
                val descriptionText = getString(R.string.notification_channel_desc)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val id = getString(R.string.notification_channel_id)
                val channel = NotificationChannel(id, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

        }
    }

    companion object{
        private lateinit var application: IApplication

        @JvmStatic
        fun instance(): IApplication{
            return application
        }
    }
}