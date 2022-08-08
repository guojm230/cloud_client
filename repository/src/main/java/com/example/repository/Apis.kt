package com.example.repository

import android.util.Log
import com.example.repository.api.Result
import com.example.repository.api.ResultCode
import com.example.repository.api.getResultCode
import com.example.repository.api.model.Account
import com.example.repository.api.model.FileItem
import com.example.repository.api.model.User
import okhttp3.*
import java.io.IOException
import java.lang.IllegalStateException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 代表一个后端可执行的Api
 */
interface Api<T> {
    fun request(): Request
}

interface TestApi<T> {
    fun request(): Request

    suspend fun call(okHttpClient: OkHttpClient): com.example.repository.api.Result<T>
}

private const val API_TAG = "com.example.repository.AbstractApi"

@Suppress("UNCHECKED_CAST")
abstract class AbstractApi<T> : TestApi<T> {

    private val returnType by lazy {
        obtainReturnType()
    }

    override suspend fun call(okHttpClient: OkHttpClient): Result<T> = suspendCoroutine { con ->
        okHttpClient.newCall(request()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(API_TAG, "onFailure: ", e)
                val result: Result<T> = when (e) {
                    is SocketTimeoutException -> Result.fail(ResultCode.NETWORK_TIMEOUT)
                    else -> Result.fail(ResultCode.NETWORK_ERROR)
                }
                con.resume(result)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body
                    if (body == null || body.contentLength() == 0L) {
                        con.resume(Result.success(null))
                    } else if (isPrimitiveReturnType()) {
                        con.resume(handlePrimitiveType(body))
                    } else {
                        con.resume(handleJsonBody(body))
                    }
                } else {
                    val body = response.body
                    if (body == null || body.contentLength() == 0L) {//http error
                        Log.e(API_TAG, "onResponse: http error${response.code}:${response.message}", )
                        con.resume(Result.fail(ResultCode.NETWORK_ERROR))
                    } else {//error with result code
                        val errorBody = gson.fromJson<ErrorBody>(body.charStream())
                        con.resume(
                            Result.fail(
                                getResultCode(errorBody.code),
                                errorBody.msg
                            )
                        )
                    }
                }
            }
        })
    }

    private fun handleJsonBody(body: ResponseBody): Result<T> {
        return try {
            val data = gson.fromJson<T>(body.charStream(), returnType)
            Result.success(data)
        } catch (e: Exception) {
            Log.e(API_TAG, "onResponse: 反序列化json为${returnType.typeName}失败", e)
            Result.fail(ResultCode.INVALID_JSON_RESULT)
        }
    }

    private fun handlePrimitiveType(body: ResponseBody): Result<T> {
        return try {
            val str = body.string()
            val data = when (returnType) {
                String::class.java -> str
                Byte::class.java -> str.toByte()
                Short::class.java -> str.toShort()
                Int::class.java -> str.toInt()
                Long::class.java -> str.toLong()
                Float::class.java -> str.toFloat()
                Double::class.java -> str.toDouble()
                Boolean::class.java -> str.toBoolean()
                else -> throw IllegalStateException("未知的primitive类型${returnType.typeName}")
            }
            Result.success(data as T)
        } catch (e: Exception) {
            Log.e(API_TAG, "handlePrimitiveType: 获取data失败", e)
            Result.fail(ResultCode.UNKNOWN_ERROR)
        }
    }

    private fun obtainReturnType(): Type {
        val cla = this::class.java
        val pt = cla.genericSuperclass as ParameterizedType
        return pt.actualTypeArguments[0]
    }


    private fun isPrimitiveReturnType(): Boolean {
        return when (returnType) {
            String::class.java,
            Byte::class.java,
            Short::class.java,
            Int::class.java,
            Long::class.java,
            Float::class.java,
            Double::class.java -> true
            else -> false
        }
    }
}

data class LoginResult(
    val token: String, val account: Account
)

class RequireCodeApi(val username: String, val loginType: Int) : Api<Map<String, String>> {
    val url = "/login"
    override fun request(): Request {
        val body = mapOf("username" to username, "loginType" to loginType.toString())
        return Request.Builder().run {
            url("${SERVER_URL}${url}")
            post(body.toJsonRequestBody())
            build()
        }
    }
}

class VerifyCodeApi(val username: String, val verifyCode: String) : Api<LoginResult> {
    val url = "/token"
    override fun request(): Request {
        val body = mapOf(
            "username" to username, "code" to verifyCode
        )
        return Request.Builder().run {
            url("${SERVER_URL}${url}")
            post(body.toJsonRequestBody())
            build()
        }
    }
}

class QueryUsersApi(val token: String) : Api<List<User>> {
    val url = "/users"
    override fun request(): Request {
        return Request.Builder().run {
            url("${SERVER_URL}${url}")
            get()
            addToken(token)
            build()
        }
    }
}

class QueryFilesApi(val path: String, val token: String) : Api<List<FileItem>> {
    val url = "/files"
    override fun request(): Request {
        return Request.Builder().run {
            url("${SERVER_URL}/${url}/${path}")
            addToken(token)
            build()
        }
    }
}

class MoveFileApi(
    val userId: Int, val token: String, val from: FileItem, val to: FileItem, val overwrite: Boolean
) : Api<Map<String, Boolean>> {
    override fun request(): Request {
        return Request.Builder().run {
            url("${SERVER_URL}/files/${userId}/move")
            addToken(token)
            post(
                mapOf(
                    "from" to from.path, "to" to to.path, "overwrite" to overwrite
                ).toJsonRequestBody()
            )
            build()
        }
    }

}

