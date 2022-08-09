package com.example.repository

import com.example.repository.api.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Request
import org.junit.Assert
import org.junit.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class JsonTest {

    val gson = Gson()

    fun <T> doMap(type: Type): T{
        val str = gson.toJson(
            listOf(
                listOf(
                    User(
                        1, 1, "123"
                    )
                )
            )
        )
        return gson.fromJson(str,type)
    }

    inline fun <reified T> map(): T{
        return doMap(object : TypeToken<T>(){}.type)
    }

    @Test
    fun test() {
        val users = map<List<List<User>>>()
        Assert.assertEquals(users[0]::class, ArrayList::class)
        Assert.assertEquals(users[0][0]::class, User::class)
    }
}