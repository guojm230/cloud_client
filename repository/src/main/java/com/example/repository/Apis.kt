package com.example.repository

import com.example.repository.api.model.Account
import com.example.repository.api.model.User
import okhttp3.Request

interface Api<T> {

    fun request(): Request

}

data class LoginResult(
    val token: String, val account: Account
)

class RequireCodeApi(val username: String, val loginType: Int) : Api<Map<String, String>> {
    val url = "/login"
    override fun request(): Request {
        val body = mapOf("username" to username, "loginType" to loginType.toString())
        return Request.Builder().run {
            url("${SERVER_URL}${url}")
            post(body.toJsonRequestBody())
            build()
        }
    }


}

class VerifyCodeApi(val username: String, val verifyCode: String) : Api<LoginResult> {
    val url = "/token"
    override fun request(): Request {
        val body = mapOf(
            "username" to username, "code" to verifyCode
        )
        return Request.Builder().run {
            url("${SERVER_URL}${url}")
            post(body.toJsonRequestBody())
            build()
        }
    }
}

class QueryUsersApi(val token: String) : Api<List<User>> {
    val url = "/users"
    override fun request(): Request {
        return Request.Builder().run {
            url("${SERVER_URL}${url}")
            get()
            header("Authorization", "Bearer ${token}")
            build()
        }
    }

}

