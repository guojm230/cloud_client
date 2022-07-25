package com.example.clouddemo.model

/**
 * 一个账户对应多个用户
 */
data class Account(
    val id: Int,
    val username: String,
    val password: String,
    val users: List<User>
)
