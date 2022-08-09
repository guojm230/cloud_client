@file:Suppress("UNCHECKED_CAST")

package com.example.base.result

sealed interface AsyncResult<T>{

    val isSuccess: Boolean
        get() = this is Success

    fun getResultOrNull(): T?{
        return when(this){
            is Success -> data
            else -> null
        }
    }

    companion object{
        @JvmStatic
        fun <T> success(data: T): AsyncResult<T>{
            return Success(data)
        }

        @JvmStatic
        fun <T> fail(code: ErrorCode): AsyncResult<T>{
            return Error(code)
        }

        @JvmStatic
        fun <T> fail(code: ErrorCode,msg: String): AsyncResult<T>{
            return Error(code, msg)
        }
    }


}

data class Success<T>(val data: T): AsyncResult<T>

data class Error<T>(
    val code: ErrorCode,
    val msg: String = code.msg
): AsyncResult<T>

inline fun <T> AsyncResult<T>.onError(handler: (ErrorCode) -> Boolean): AsyncResult<T>{
    if (this is Error<T> && !handler(code)){
        GlobalErrorHandler.postErrorCode(this)
    }
    return this
}

inline fun <T> AsyncResult<T>.runPostError(handler: (T) -> Unit){
    when(this){
        is Success<T> -> handler(data)
        is Error<T> -> GlobalErrorHandler.postErrorCode(this)
    }
}

inline fun <T, R> AsyncResult<T>.map(mapper: (T)->R): AsyncResult<R> {
    return when(this){
        is Success -> AsyncResult.success(mapper(data))
        else -> this as AsyncResult<R>
    }
}


inline fun <T> AsyncResult<T>.onSuccess(handler: (T)->Unit): AsyncResult<T>{
    if (this is Success<T>){
        handler(data)
    }
    return this
}