package com.example.repository

import com.example.repository.api.Result
import com.example.repository.api.ResultCode.NETWORK_ERROR
import com.example.repository.api.getResultCode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.io.Reader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal const val SERVER_HOST = "guojm.link"
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

suspend inline fun <reified T> OkHttpClient.callApi(api: Api<T>): Result<T> {
    return call(api.request())
}

suspend inline fun <reified T> OkHttpClient.call(request: Request): Result<T> =
    suspendCoroutine { con ->
        newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                con.resume(Result.fail(NETWORK_ERROR))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body
                    if (body == null) {
                        con.resume(Result.success(null))
                    } else {
                        val data = gson.fromJson<T>(body.charStream())
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