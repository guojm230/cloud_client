package com.example.repository.api

/**
 * 结果返回码，全局通用
 * http码后扩展3位
 * 每个错误码必须包含一个默认消息，作为兜底向用户展示的消息
 */
enum class ResultCode(val code: Int, val msg: String) {

    SUCCESS(200000, "OK"),

    INVALID_PARAM(400000, "非法参数"), VERIFY_CODE_ERROR(400001, "验证码错误"), INVALID_TOKEN(
        401001, "Token超时或错误"
    ),

    NOT_FOUND_RESOURCE(404000, "资源不存在"), NOT_FOUND_USER(404001, "用户不存在"), NOT_FOUND_FILE(
        404002,
        "文件不存在"
    ),

    //客户端侧全局错误，如IO错误之类的
    NETWORK_ERROR(400100, "网络错误，请稍后再试"), NETWORK_TIMEOUT(400101, "网络请求超时，请稍后再试"), UNKNOWN_ERROR(
        400110, "未知错误"
    ),

    UNKNOWN_SERVER_ERROR(500000, "服务器未知错误"),
}

fun getResultCode(code: Int): ResultCode {
    var c = code
    if (code < 600) {
        c *= 1000
    }
    return ResultCode.values().find { it.code == c }!!
}
