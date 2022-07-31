package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private var currentLoginAccount: LoginAccount? = null
    private var liveCurrentUser: MutableLiveData<User> = MutableLiveData()
    private var liveCurrentAccount: MutableLiveData<Account> = MutableLiveData()

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

    override suspend fun currentAccount(): Account? {
        if (currentLoginAccount != null) {
            val account = currentLoginAccount!!.toAccount()
            if (liveCurrentAccount.value?.id != currentLoginAccount!!.id) {
                liveCurrentAccount.postValue(account)
            }
            return account
        }
        currentLoginAccount = findLoginAccount()
        if (currentLoginAccount != null) {
            liveCurrentAccount.postValue(currentLoginAccount!!.toAccount())
            return currentLoginAccount!!.toAccount()
        }
        return null
    }

    override suspend fun liveCurrentAccount(): LiveData<Account> {
        return liveCurrentAccount
    }

    override suspend fun queryLoginAccounts(): List<Account> {
        return accountDao.queryAllAccounts().map(LoginAccount::toAccount)
    }

    override suspend fun queryUsers(): Result<List<User>> {
        if (findLoginAccount() == null) {
            return Result.fail(INVALID_TOKEN)
        }
        return okHttpClient.callApi(QueryUsersApi(currentLoginAccount!!.token))
    }

    override suspend fun setCurrentUser(user: User) {
        currentUser = user
        loginInfoSP.edit().run {
            putString(SP_KEY_LOGIN_USER, gson.toJson(user))
            commit()
        }
        if (user.id != liveCurrentUser.value?.id) {
            liveCurrentUser.postValue(currentUser)
        }
    }

    override suspend fun currentUser(): User? {
        if (currentUser != null) {
            return currentUser
        }
        currentAccount() ?: return null
        if (!loginInfoSP.contains(SP_KEY_LOGIN_USER)) {
            return null
        }
        currentUser =
            gson.fromJson(loginInfoSP.getString(SP_KEY_LOGIN_USER, null), User::class.java)
        liveCurrentUser.postValue(currentUser)
        return currentUser
    }

    override suspend fun liveCurrentUser(): LiveData<User> {
        return liveCurrentUser
    }

    override fun currentAccountToken(): String? {
        return findLoginAccount()?.token
    }

    private fun findLoginAccount(): LoginAccount? {
        if (currentLoginAccount != null) {
            return currentLoginAccount
        }
        if (!loginInfoSP.contains(SP_KEY_LOGIN_ACCOUNT)) {
            return null
        }
        val loginAccountStr = loginInfoSP.getString(SP_KEY_LOGIN_ACCOUNT, "")
        currentLoginAccount = gson.fromJson(loginAccountStr, LoginAccount::class.java)
        return currentLoginAccount
    }

    override suspend fun setCurrentAccount(account: Account) {
        currentLoginAccount = accountDao.findAccountById(account.id)
        loginInfoSP.edit().run {
            putString(SP_KEY_LOGIN_ACCOUNT, gson.toJson(currentLoginAccount))
            commit()
        }
        liveCurrentAccount.postValue(account)
    }

    override suspend fun logout(account: Account) {
        if (currentLoginAccount != null && currentLoginAccount!!.id == account.id) {
            loginInfoSP.edit().run {
                remove(SP_KEY_LOGIN_USER)
                remove(SP_KEY_LOGIN_ACCOUNT)
                commit()
            }
            accountDao.deleteAccount(currentLoginAccount!!)
        } else {
            accountDao.deleteAccountById(account.id)
        }
    }

    override suspend fun isAuthenticated(): Boolean = findLoginAccount() != null
}