package com.example.repository.dependency

import android.content.Context
import androidx.room.Room
import com.example.repository.FileRepositoryImpl
import com.example.repository.UserRepositoryImpl
import com.example.repository.api.*
import com.example.repository.dao.AccountDao
import com.example.repository.dao.AppDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.time.Duration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HiltModule {

    companion object {
        @Provides
        @Singleton
        @JvmStatic
        fun httpClient(): OkHttpClient {
            return OkHttpClient.Builder().run {
                connectTimeout(Duration.ofSeconds(20))
                build()
            }
        }

        @Provides
        @Singleton
        @JvmStatic
        fun appDataBase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "cloud-database").build()
        }

        @Provides
        @Singleton
        @JvmStatic
        fun accountDao(appDatabase: AppDatabase): AccountDao {
            return appDatabase.accountDao()
        }

    }


    @Binds
    @Singleton
    abstract fun userRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun fileRepository(fileRepository: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun fileApi(fileApiImpl: FileApiImpl): IFileApi

    @Binds
    @Singleton
    abstract fun userApi(userApiImpl: UserApiImpl): IUserApi


}