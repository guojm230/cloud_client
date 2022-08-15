package com.example.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.repository.api.UserApiImpl
import com.example.repository.api.UserRepository
import com.example.repository.api.model.Account
import com.example.repository.api.model.User
import com.example.repository.dao.AccountDao
import com.example.repository.dao.AppDatabase
import com.example.repository.dao.entity.LoginAccount
import com.example.repository.dependency.HiltModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryTest {

    private lateinit var accountDao: AccountDao
    private lateinit var appDatabase: AppDatabase
    private lateinit var userRepository: UserRepository


    val account = Account(
        1,
        "13837109739",
        "2306227382@qq.com"
    )

    val user = User(
        1,
        1,
        "guojm"
    )

    @Before
    fun prepareEnv() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        accountDao = appDatabase.accountDao()
        val userApiImpl = UserApiImpl(HiltModule.httpClient())
        userRepository = UserRepositoryImpl(context, userApiImpl, accountDao)
        accountDao.addAccount(
            LoginAccount(
                account.id,
                account.tel,
                account.email,
                "token",
                System.currentTimeMillis()
            )
        )
    }

    @Test
    fun userTest() = runBlocking(Dispatchers.Main) {
        var count = 0
        userRepository.run {
            liveCurrentAccount().observeForever {
                count++
            }
            setCurrentAccount(account)
            Assert.assertTrue(isAuthenticated())
            setCurrentUser(user)
            Assert.assertTrue(currentUser()!!.id == 1)
            logout(account)
            Assert.assertFalse(isAuthenticated())
            Assert.assertTrue(currentUser() == null)
            //两次监听到当前账户的变化
            //给一定的延迟时间执行回调
            delay(100)
            Assert.assertEquals(2, count)
        }
    }

    @After
    fun close() {
        appDatabase.close()
    }

}