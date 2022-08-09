package com.example.user.model

import com.example.user.components.LoginType

data class LoginData(
    val loginType: LoginType, val username: String, val password: String? = null
)