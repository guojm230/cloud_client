package com.example.repository.api

import androidx.lifecycle.LiveData
import com.example.base.result.AsyncResult
import com.example.repository.api.model.Account
import com.example.repository.api.model.User

interface UserRepository {
    /**
     * 由账号获取随机的6位验证码
     * @param username tel or email
     * @param loginType 1:tel or 2:email
     * @return 6位随机数字验证码
     */
    suspend fun requireVerifyCode(username: String, loginType: Int): AsyncResult<String>

    /**
     * 提交验证码，通过验证码登录
     * 该方法会默认将账号信息添加到本地的登录列表中
     * @param username 邮箱/手机号
     * @param verifyCode 对应的6位数字验证码
     * @param saveLocal 是否保存到本地登录列表中
     * @return 返回对应的账户信息
     */
    suspend fun verifyCode(
        username: String, verifyCode: String, saveLocal: Boolean = true
    ): AsyncResult<Account>

    /**
     * 返回当前登录的账户信息
     * @return 当前登录的账户信息，未登录则返回null
     */
    fun currentAccount(): Account?

    /**
     * 返回可订阅的当前账户，可用于监听账户切换
     * 注意：由于postValue带来的延迟性，LiveData::value获取的值不具有实时性
     * @return 可订阅的当前账户
     */
    fun liveCurrentAccount(): LiveData<Account>

    /**
     * @return 返回本地保存的所有登录过的账户信息
     */
    suspend fun queryLoginAccounts(): List<Account>

    /**
     * @return 查询登录账户对应的用户列表
     */
    suspend fun queryUsers(): AsyncResult<List<User>>

    /**
     * 将当前选择的User保存到本地
     */
    suspend fun setCurrentUser(user: User)

    /**
     * 当前登录账户选择的User
     * 若用户尚未选择角色，返回null
     * @return 当前账户选择的User 或 null
     */
    fun currentUser(): User?


    /**
     * 返回可订阅的当前用户，可用于监听用户切换
     * 注意：由于postValue带来的延迟性，LiveData::value获取的值不具有实时性
     */
    fun liveCurrentUser(): LiveData<User>


    /**
     * @return 当前登录者的token，未登录则返回null
     */
    fun currentAccountToken(): String?

    /**
     * 切换登录用户
     * @return
     */
    suspend fun setCurrentAccount(account: Account)

    /**
     * 删除本地储存的账户信息
     */
    suspend fun logout(account: Account)

    /**
     * 用户是否已经认证过
     * @return 是否已经认证
     */
    suspend fun isAuthenticated(): Boolean

}