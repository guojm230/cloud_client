package com.example.repository.api.exceptions

open class ApiException(
    msg: String = "unknown error"
): RuntimeException(msg,null,false,false)