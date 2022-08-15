package com.example.repository

import com.example.base.result.onSuccess
import com.example.repository.dependency.HiltModule
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.junit.Assert
import org.junit.Test


class ApiTest {

    @Test
    fun callPrimitiveTest(): Unit = runBlocking {
        val request = Request.Builder().run {
            withBaseUrl("/hello")
            build()
        }
        val str = HiltModule.httpClient().call<String>(request)
        str.onSuccess {
            assert(it == "hello")
        }
    }

    @Test
    fun callJsonTest(): Unit = runBlocking {
        val request = Request.Builder().run {
            withBaseUrl("/verify_code")
            post(
                mapOf(
                    "username" to "13837109739",
                    "loginType" to "tel"
                ).toJsonRequestBody()
            )
            build()
        }
        val str = HiltModule.httpClient().call<Map<String, String>>(request)
        str.onSuccess {
            Assert.assertTrue(it.containsKey("code"))
        }
    }

}