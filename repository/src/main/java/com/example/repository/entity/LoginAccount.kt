package com.example.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.repository.api.model.Account

@Entity(tableName = "account")
data class LoginAccount(
    @PrimaryKey val id: Int, val tel: String, val email: String, val token: String, //登录时间
    @ColumnInfo(name = "login_time") val loginTime: Long
)

fun LoginAccount.toAccount(): Account = Account(
    id, tel, email
)
