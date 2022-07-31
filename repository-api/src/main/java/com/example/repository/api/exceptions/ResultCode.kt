package com.example.repository.api.exceptions

enum class ResultCode(val code: Int,val msg: String) {

    NOT_FOUND_USER(404001,"");

}

fun ResultCode.forValue(code: Int): ResultCode?{
    return ResultCode.values().find { it.code == code }
}