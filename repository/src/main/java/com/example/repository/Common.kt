package com.example.repository

import com.example.repository.api.Result
import com.example.repository.api.ResultCode.NETWORK_ERROR
import com.example.repository.api.ResultCode.NETWORK_TIMEOUT
import com.example.repository.api.getResultCode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.Reader
import java.net.SocketTimeoutException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal const val SERVER_HOST = "10.83.241.40"
internal const val SERVER_PORT = 8080
internal const val PROTOCOL = "http"
internal const val SERVER_URL = "${PROTOCOL}://${SERVER_HOST}:${SERVER_PORT}"

internal const val SP_LOGIN_INFO = "org.example.cloud.loginInfo"

internal const val SP_KEY_LOGIN_ACCOUNT = "loginAccount"
internal const val SP_KEY_LOGIN_USER = "loginUser"

val jsonType = "application/json".toMediaType()
val textType = "text/*".toMediaType()

val gson = Gson()

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
 * gson反序列化时通过class传入信息，只能得到List::class，无法进一步获取其中的User类型,List<User>::class
 * 最终反序列化的结果为List<Map>类型,显然不符合。
 * kotlin中的inline <reified T>函数可以保留泛型信息，但inline函数无法调试，代码量也会膨胀
 * 所以我们只在其中构造好mapper的lambda表达式，捕获到泛型信息后就再调用其它函数，减少展开的代码量和方便调试
 */
suspend inline fun <reified T> OkHttpClient.callApi(api: Api<T>): Result<T> {
    val jsonMapper = { body: ResponseBody->
        gson.fromJson<T>(body.charStream())
    }
    return call(api.request(),jsonMapper)
}

suspend fun <T> OkHttpClient.call(request: Request,jsonMapper:(ResponseBody)->T): Result<T> =
    suspendCoroutine { con ->
        newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val result: Result<T> = when(e){
                    is SocketTimeoutException-> Result.fail(NETWORK_TIMEOUT)
                    else -> Result.fail(NETWORK_ERROR)
                }
                con.resume(result)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body
                    if (body == null) {
                        con.resume(Result.success(null))
                    } else {
                        val data = jsonMapper(body)
                        con.resume(Result.success(data))
                    }
                } else {
                    val body = response.body
                    if (body == null) {
                        con.resume(Result.fail(getResultCode(response.code), response.message))
                    } else {
                        val errorBody = gson.fromJson<ErrorBody>(body.charStream())
                        con.resume(Result.fail(getResultCode(errorBody.code), errorBody.msg))
                    }
                }
            }
        })
    }