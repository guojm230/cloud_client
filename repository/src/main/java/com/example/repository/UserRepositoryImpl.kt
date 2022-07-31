package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.repository.api.Result
import com.example.repository.api.ResultCode.INVALID_TOKEN
import com.example.repository.api.UserRepository
import com.example.repository.api.model.Account
import com.example.repository.api.model.User
import com.example.repository.dao.AccountDao
import com.example.repository.entity.LoginAccount
import com.example.repository.entity.toAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context,
    val okHttpClient: OkHttpClient,
    val accountDao: AccountDao
) : UserRepository {

    private var currentUser: User? = null
    private var currentAccount: LoginAccount? = null

    private val loginInfoSP: SharedPreferences by lazy {
        context.getSharedPreferences(SP_LOGIN_INFO, Context.MODE_PRIVATE)
    }

    override suspend fun requireVerifyCode(username: String, loginType: Int): Result<String> {
        val result = okHttpClient.callApi(RequireCodeApi(username, loginType))
        return result.map { it!!["code"] as String }
    }

    override suspend fun verifyCode(
        username: String, verifyCode: String, saveLocal: Boolean
    ): Result<Account> {
        val result = okHttpClient.callApi(VerifyCodeApi(username, verifyCode))
        if (result.isSuccess) {
            val account = result.data!!.account
            val existAccount = accountDao.findAccountById(account.id)
            if (existAccount == null) {
                accountDao.addAccount(
                    LoginAccount(
                        id = account.id,
                        tel = account.tel,
                        email = account.email,
                        token = result.data!!.token,
                        loginTime = System.currentTimeMillis()
                    )
                )
            }
        }
        return result.map { it?.account }
    }

    override suspend fun queryCurrentAccount(): Account? {
        if (currentAccount != null) {
            return currentAccount!!.toAccount()
        }
        if (!loginInfoSP.contains(SP_KEY_LOGIN_ACCOUNT)) {
            return null
        }
        val loginAccountStr = loginInfoSP.getString(SP_KEY_LOGIN_ACCOUNT, "")
        return gson.fromJson(loginAccountStr, LoginAccount::class.java).toAccount()
    }

    override suspend fun queryLoginAccounts(): List<Account> {
        return accountDao.queryAllAccounts().map(LoginAccount::toAccount)
    }

    override suspend fun queryUsers(): Result<List<User>> {
        if (currentAccount == null) {
            return Result.fail(INVALID_TOKEN)
        }
        return okHttpClient.callApi(QueryUsersApi(currentAccount!!.token))
    }

    override suspend fun setCurrentUser(user: User) {
        currentUser = user
        loginInfoSP.edit().run {
            putString(SP_KEY_LOGIN_USER, gson.toJson(user))
            commit()
        }
    }

    override suspend fun currentUser(): User? {
        if (currentUser != null) {
            return currentUser
        }
        queryCurrentAccount() ?: return null
        if (!loginInfoSP.contains(SP_KEY_LOGIN_USER)) {
            return null
        }
        return gson.fromJson(loginInfoSP.getString(SP_KEY_LOGIN_USER, null), User::class.java)
    }

    override suspend fun setCurrentAccount(account: Account) {
        currentAccount = accountDao.findAccountById(account.id)
        loginInfoSP.edit().run {
            putString(SP_KEY_LOGIN_ACCOUNT, gson.toJson(currentAccount))
            commit()
        }
    }

    override suspend fun logout(account: Account) {
        if (currentAccount != null && currentAccount!!.id == account.id) {
            loginInfoSP.edit().run {
                remove(SP_KEY_LOGIN_USER)
                remove(SP_KEY_LOGIN_ACCOUNT)
                commit()
            }
            accountDao.deleteAccount(currentAccount!!)
        } else {
            accountDao.deleteAccountById(account.id)
        }
    }

    override suspend fun isAuthenticated(): Boolean = queryCurrentAccount() == null
}