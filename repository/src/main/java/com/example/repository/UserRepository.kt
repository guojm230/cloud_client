package com.example.repository

import android.content.Context
import com.example.repository.api.UserRepository
import com.example.repository.api.model.Account
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    @Inject @ApplicationContext
    val context: Context,
    @Inject
    val okHttpClient: OkHttpClient
): UserRepository{

    override suspend fun requireVerifyCode(tel: String): String {
        
        return ""
    }

    override suspend fun verifyCode(tel: String, verifyCode: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun queryAccount(forceRefresh: Boolean): Account {
        TODO("Not yet implemented")
    }

    override suspend fun isAuthenticated(): Boolean {
        TODO("Not yet implemented")
    }
}