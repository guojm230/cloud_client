package com.example.repository.api

import com.example.repository.api.model.Account


interface UserRepository {
    /**
     * 由手机号获取随机的验证码
     * @param tel 手机号
     * @return 6位随机数字验证码
     * @throws
     */
    suspend fun requireVerifyCode(tel: String): String

    /**
     * 验证对应的验证码
     * @param tel 手机号
     * @param verifyCode 对应的6位数字验证码
     * @return 返回对应的token
     */
    suspend fun verifyCode(tel: String,verifyCode: String): String

    /**
     * 返回已登录用户的账户信息
     * 该方法必须已经登录过的用户才能够使用，否则抛出异常
     *
     */
    suspend fun queryAccount(forceRefresh: Boolean = false): Account

    /**
     * 用户是否已经认证过
     * @return 是否已经认证
     */
    suspend fun isAuthenticated(): Boolean

}