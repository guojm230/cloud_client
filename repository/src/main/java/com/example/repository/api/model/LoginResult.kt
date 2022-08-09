package com.example.repository.api.model

data class LoginResult(
    val token: String,
    val account: Account
)