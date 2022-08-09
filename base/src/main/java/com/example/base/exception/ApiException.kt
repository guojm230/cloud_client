package com.example.base.exception

/**
 * 表示接口错误的异常,只用来返回错误信息，不携带栈信息
 * 必须包含提示信息
 */
open class ApiException(msg: String): RuntimeException(
    msg,null,false,false
)

object NotFoundException: ApiException("未找到对应资源")
object AuthorizedException: ApiException("认证失败")
object NetworkTimeoutException: ApiException("")

fun findExceptionByCode(code: Int): ApiException{
    return NotFoundException
}