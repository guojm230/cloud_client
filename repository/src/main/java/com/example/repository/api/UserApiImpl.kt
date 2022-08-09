package com.example.repository.api

import com.example.repository.api.model.User
import com.example.repository.call
import com.example.base.result.AsyncResult
import com.example.base.result.map
import com.example.repository.addToken
import com.example.repository.api.model.LoginResult
import com.example.repository.toJsonRequestBody
import com.example.repository.withBaseUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class UserApiImpl @Inject constructor(
    val okHttpClient: OkHttpClient
): IUserApi {

    override suspend fun requireVerifyCode(username: String, loginType: Int): AsyncResult<String> {
        val body = mapOf("username" to username, "loginType" to loginType.toString())
        val request = Request.Builder().run {
            withBaseUrl("/verify_code")
            post(body.toJsonRequestBody())
            build()
        }
        return okHttpClient.call<Map<String,String>>(request).map { it["code"]!! }
    }

    override suspend fun accessToken(username: String, code: String): AsyncResult<LoginResult> {
        val body = mapOf(
            "username" to username, "code" to code
        )
        val request = Request.Builder().run {
            withBaseUrl("/access_token")
            post(body.toJsonRequestBody())
            build()
        }
        return okHttpClient.call(request)
    }

    override suspend fun queryUsers(token: String): AsyncResult<List<User>> {
        val request = Request.Builder().run {
            withBaseUrl("/users")
            addToken(token)
            build()
        }
        return okHttpClient.call(request)
    }
}