package com.example.repository.api

import com.example.repository.api.ResultCode.SUCCESS

/**
 * web层返回的结果类
 */
data class Result<T>(
    val code: ResultCode, val data: T?, val msg: String = ""
) {

    val isSuccess get() = code == SUCCESS

    fun <R> map(mapper: ((T?) -> R?)): Result<R> {
        if (isSuccess) {
            return success(mapper(data))
        }
        return fail(code, msg)
    }

    companion object {
        @JvmStatic
        fun <T> success(data: T?): Result<T> {
            return Result(SUCCESS, data)
        }

        fun <T> fail(code: ResultCode, msg: String? = null): Result<T> {
            return Result(code, null, msg ?: code.msg)
        }
    }

}