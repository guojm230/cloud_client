package com.example.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.repository.entity.LoginAccount

@Dao
interface AccountDao {

    @Insert
    suspend fun addAccount(account: LoginAccount)

    @Delete
    suspend fun deleteAccount(account: LoginAccount)

    @Query("DELETE FROM account WHERE id = :id")
    suspend fun deleteAccountById(id: Int)

    @Query("SELECT * FROM account ORDER BY login_time")
    suspend fun queryAllAccounts(): List<LoginAccount>

    @Query("SELECT * FROM account WHERE id = :id")
    suspend fun findAccountById(id: Int): LoginAccount?

}