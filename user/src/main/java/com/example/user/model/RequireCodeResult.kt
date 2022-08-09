package com.example.user.model

data class RequireCodeResult(
    val success: Boolean,
    val code: String = "",
    val notify: Boolean = true,
    val errorMsg: String = ""
)