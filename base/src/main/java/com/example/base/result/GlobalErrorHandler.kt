package com.example.base.result

import android.os.Handler
import android.os.Looper
import java.util.*

typealias ErrorCodeHandler = (Error<*>)->Boolean

/**
 * 全局的ErrorCode处理器
 * 注册handler，当遇到未处理的错误时会按照顺序调用拦截器，
 * 直到遇到返回结果为true的handler
 */
object GlobalErrorHandler {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val handlers = LinkedList<ErrorCodeHandler>()

    fun postErrorCode(error: Error<*>){
        mainHandler.post {
            handlers.forEach { handler->
                if (handler(error)){
                    return@forEach
                }
            }
        }
    }

    fun addLastHandler(handler: ErrorCodeHandler){
        handlers.addLast(handler)
    }

    fun addFirstHandler(handler: ErrorCodeHandler){
        handlers.addFirst(handler)
    }

    fun removeHandler(handler: ErrorCodeHandler){
        handlers.remove(handler)
    }

}