package com.example.repository.api

import com.example.repository.api.model.User
import com.example.base.result.AsyncResult
import com.example.repository.api.model.LoginResult

interface IUserApi {

    suspend fun requireVerifyCode(username: String,loginType: Int): AsyncResult<String>

    /**
     * 获取token
     */
    suspend fun accessToken(username: String,code: String): AsyncResult<LoginResult>

    /**
     * 查询账户的user
     */
    suspend fun queryUsers(token: String): AsyncResult<List<User>>
}