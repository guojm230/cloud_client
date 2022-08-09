package com.example.base.result


/**
 * 结果返回码，全局通用
 * http码后扩展3位
 * 每个错误码必须包含一个默认消息，作为兜底向用户展示的消息
 */
enum class ErrorCode(val code: Int, val msg: String) {

    BAD_REQUEST(400000,"错误请求"),
    VERIFY_CODE_ERROR(400002, "验证码错误"),
    INVALID_TOKEN(401001, "Token超时或错误"),
    NOT_FOUND(404000, "资源不存在"),

    UNKNOWN_ERROR(400510, "未知错误"),

    FILE_EXITS_ERROR(409001,"文件已经存在"),

    UNKNOWN_SERVER_ERROR(500000, "服务器未知错误"),
    INVALID_JSON_RESULT(500001,"返回结果格式错误"),

    NETWORK_ERROR(600000, "网络错误，请稍后再试"),
    NETWORK_TIMEOUT(600001, "网络请求超时，请稍后再试"),
}

fun findResultCode(code: Int): ErrorCode {
    //规格化为6位数字
    val canonicalCode = if (code <= 999){
        code*1000
    } else {
        code
    }
    val resultCode = ErrorCode.values().find { it.code == canonicalCode } ?: when(canonicalCode/100000){
        4 -> ErrorCode.BAD_REQUEST
        5 -> ErrorCode.UNKNOWN_SERVER_ERROR
        6 -> ErrorCode.NETWORK_ERROR
        else -> throw IllegalArgumentException("错误的error code格式")
    }
    return resultCode
}
