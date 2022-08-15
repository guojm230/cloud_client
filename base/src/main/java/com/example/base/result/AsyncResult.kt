@file:Suppress("UNCHECKED_CAST")

package com.example.base.result

/**
 * 代表异步返回的结果
 */
sealed interface AsyncResult<T> {

    val isSuccess: Boolean
        get() = this is Success

    fun getResultOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }

    companion object {
        @JvmStatic
        fun <T> success(data: T): AsyncResult<T> {
            return Success(data)
        }

        @JvmStatic
        fun <T> fail(
            code: ErrorCode,
            e: Throwable? = null,
            msg: String? = e?.message
        ): AsyncResult<T> {
            return Error(code, msg ?: code.msg, e)
        }

        @JvmStatic
        fun <T> fail(code: ErrorCode, msg: String): AsyncResult<T> {
            return Error(code, msg)
        }

        @JvmStatic
        fun <T> fail(e: Throwable, msg: String? = e.message): AsyncResult<T> {
            return Error(ErrorCode.UNKNOWN_ERROR, msg ?: ErrorCode.UNKNOWN_ERROR.msg, throwable = e)
        }

        @JvmStatic
        inline fun <T> runCatch(block: () -> T): AsyncResult<T> {
            return try {
                success(block())
            } catch (e: Exception) {
                fail(e)
            }
        }
    }

}

class Success<T>(val data: T) : AsyncResult<T>

class Error<T>(
    val code: ErrorCode,
    val msg: String = code.msg,
    val throwable: Throwable? = null
) : AsyncResult<T>

inline fun <T> AsyncResult<T>.onError(handler: (Error<T>) -> Unit): AsyncResult<T> {
    if (this is Error<T>) {
        handler(this)
    }
    return this
}

inline fun <T> AsyncResult<T>.catchAllError(handler: (Error<T>) -> Boolean): AsyncResult<T> {
    if (this is Error && !handler(this)) {
        GlobalErrorHandler.postErrorCode(this)
    }
    return this
}

inline fun <T> AsyncResult<T>.catchError(
    vararg errors: ErrorCode,
    handler: (Error<T>) -> Unit
): AsyncResult<T> {
    if (this is Error) {
        if (errors.any { it == code }) {
            handler(this)
        } else {
            GlobalErrorHandler.postErrorCode(this)
        }
    }
    return this
}

/**
 * post所有error
 */
inline fun <T> AsyncResult<T>.runPostError(handler: (T) -> Unit) {
    when (this) {
        is Success<T> -> handler(data)
        is Error<T> -> GlobalErrorHandler.postErrorCode(this)
    }
}

inline fun <T, R> AsyncResult<T>.map(mapper: (T) -> R): AsyncResult<R> {
    return when (this) {
        is Success -> AsyncResult.success(mapper(data))
        else -> this as AsyncResult<R>
    }
}


inline fun <T> AsyncResult<T>.onSuccess(handler: (T) -> Unit): AsyncResult<T> {
    if (this is Success<T>) {
        handler(data)
    }
    return this
}