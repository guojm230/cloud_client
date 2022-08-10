@file:Suppress("UNCHECKED_CAST")

package com.example.repository

import android.util.Log
import com.example.base.result.AsyncResult
import com.example.base.result.ErrorCode
import com.example.base.result.ErrorCode.NETWORK_ERROR
import com.example.base.result.findResultCode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.Reader
import java.lang.IllegalStateException
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal const val SERVER_HOST = "192.168.10.148"
internal const val SERVER_PORT = 8080
internal const val PROTOCOL = "http"
internal const val SERVER_URL = "${PROTOCOL}://${SERVER_HOST}:${SERVER_PORT}"

internal const val SP_LOGIN_INFO = "org.example.cloud.loginInfo"

internal const val SP_KEY_LOGIN_ACCOUNT = "loginAccount"
internal const val SP_KEY_LOGIN_USER = "loginUser"

val jsonType = "application/json".toMediaType()
val textType = "text/*".toMediaType()

val gson = Gson()

private const val API_TAG = "com.example.repository.api"

inline fun <reified T> Gson.fromJson(str: String): T {
    return fromJson(str, (object : TypeToken<T>() {}).type)
}

inline fun <reified T> Gson.fromJson(reader: Reader): T {
    return fromJson(reader, (object : TypeToken<T>() {}).type)
}

fun Any.toJsonRequestBody(): RequestBody {
    return gson.toJson(this).toRequestBody(jsonType)
}

fun Request.Builder.addToken(token: String) {
    header("Authorization", "Bearer $token")
}


/**
 * 由于JVM的类型擦除，对于复杂的泛型结构，如：List<User>
 * gson反序列化时通过class传入信息，只能得到List::class，无法进一步获取其中的User类型:List<User>::class
 * 最终反序列化的结果为List<Map>类型,不符合我们的要求。
 * kotlin中的inline <reified T>函数可以保留泛型信息，但inline函数无法调试，代码量也会膨胀
 * 所以我们只在其中捕获到泛型信息后就再调用其它函数，减少展开的代码量和方便调试
 */
suspend inline fun <reified T> OkHttpClient.call(request: Request): AsyncResult<T> {
    return call(request,object: TypeToken<T>(){}.type)
}


@Suppress("UNCHECKED_CAST")
suspend fun <T> OkHttpClient.call(request: Request, returnType: Type): AsyncResult<T> =
    suspendCoroutine { con ->
        newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(API_TAG, "onFailure: 网络请求异常", e)
                val asyncResult: AsyncResult<T> = when (e) {
                    is SocketTimeoutException -> AsyncResult.fail(ErrorCode.NETWORK_TIMEOUT)
                    else -> AsyncResult.fail(ErrorCode.NETWORK_ERROR)
                }
                con.resume(asyncResult)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body
                    if (body == null || body.contentLength() == 0L) {
                        con.resume(AsyncResult.success(null as T))
                    } else if (isPrimitiveType(returnType)) {
                        con.resume(handlePrimitiveType(body,returnType))
                    } else {
                        con.resume(handleJsonBody(body,returnType))
                    }
                } else {
                    val body = response.body
                    if (body == null || body.contentLength() == 0L) {
                        val httpCode = response.code
                        Log.d(API_TAG, "onResponse:$httpCode : ${request.url}")
                        con.resume(AsyncResult.fail(findResultCode(httpCode)))
                    } else {//error with ErrorBody
                        val errorBody = gson.fromJson<ErrorBody>(body.charStream())
                        con.resume(
                            AsyncResult.fail(
                                findResultCode(errorBody.code),errorBody.msg
                            )
                        )
                    }
                }
            }
        })
    }

private fun <T> handlePrimitiveType(body: ResponseBody,returnType: Type): AsyncResult<T> {
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
        AsyncResult.success(data as T)
    } catch (e: Exception) {
        Log.e(API_TAG, "handlePrimitiveType: 获取data失败", e)
        AsyncResult.fail(ErrorCode.UNKNOWN_ERROR)
    }
}

private fun <T> handleJsonBody(body: ResponseBody,returnType: Type): AsyncResult<T> {
    return try {
        val data = gson.fromJson<T>(body.charStream(), returnType)
        AsyncResult.success(data)
    } catch (e: Exception) {
        Log.e(API_TAG, "onResponse: 反序列化json为${returnType.typeName}失败", e)
        AsyncResult.fail(ErrorCode.INVALID_JSON_RESULT)
    }
}

private fun isPrimitiveType(returnType: Type): Boolean{
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

fun Request.Builder.withBaseUrl(relativeUrl: String){
    val u = relativeUrl.run {
        if (startsWith("/")) this else "/$this"
    }
    url("$SERVER_URL$u")
}