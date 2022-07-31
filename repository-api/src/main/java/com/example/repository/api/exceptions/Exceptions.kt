package com.example.repository.api.exceptions

class UserNotFoundException(msg:String = "用户不存在"): ApiException(msg)

class VerifyCodeErrorException(): ApiException("验证码错误")
