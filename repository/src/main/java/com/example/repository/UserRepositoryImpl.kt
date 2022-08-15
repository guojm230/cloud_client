package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.base.result.AsyncResult
import com.example.base.result.ErrorCode.INVALID_TOKEN
import com.example.base.result.map
import com.example.base.result.onSuccess
import com.example.repository.api.IUserApi
import com.example.repository.api.UserRepository
import com.example.repository.api.model.Account
import com.example.repository.api.model.User
import com.example.repository.dao.AccountDao
import com.example.repository.dao.entity.LoginAccount
import com.example.repository.dao.entity.toAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context,
    val userApi: IUserApi,
    val accountDao: AccountDao
) : UserRepository {

    private var currentUser: User? = null

    private var currentLoginAccount: LoginAccount? = null
    private var liveCurrentUser: MutableLiveData<User> = MutableLiveData()
    private var liveCurrentAccount: MutableLiveData<Account> = MutableLiveData()

    private val loginInfoSP: SharedPreferences by lazy {
        context.getSharedPreferences(SP_LOGIN_INFO, Context.MODE_PRIVATE)
    }

    override suspend fun requireVerifyCode(username: String, loginType: Int): AsyncResult<String> {
        return userApi.requireVerifyCode(username, loginType)
    }

    override suspend fun verifyCode(
        username: String, verifyCode: String, saveLocal: Boolean
    ): AsyncResult<Account> {
        val result = userApi.accessToken(username, verifyCode)
        return result.onSuccess {
            val account = it.account
            val existAccount = accountDao.findAccountById(account.id)
            if (existAccount == null) {
                accountDao.addAccount(
                    LoginAccount(
                        id = account.id,
                        tel = account.tel,
                        email = account.email,
                        token = it.token,
                        loginTime = System.currentTimeMillis()
                    )
                )
            }
        }.map { it.account }
    }

    override fun currentAccount(): Account? {
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

    override fun liveCurrentAccount(): LiveData<Account> {
        return liveCurrentAccount
    }

    override suspend fun queryLoginAccounts(): List<Account> {
        return accountDao.queryAllAccounts().map(LoginAccount::toAccount)
    }

    override suspend fun queryUsers(): AsyncResult<List<User>> {
        if (findLoginAccount() == null) {
            return AsyncResult.fail(INVALID_TOKEN)
        }
        return userApi.queryUsers(currentAccountToken()!!)
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

    override fun currentUser(): User? {
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

    override fun liveCurrentUser(): LiveData<User> {
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
        liveCurrentAccount.postValue(null)
        liveCurrentUser.postValue(null)
        currentLoginAccount = null
        currentUser = null
    }

    override fun isAuthenticated(): Boolean = findLoginAccount() != null
}