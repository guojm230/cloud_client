package com.example.repository

import com.example.repository.api.model.User
import com.google.gson.Gson
import okhttp3.Request
import org.junit.Assert
import org.junit.Test

class JsonTest {

    val gson = Gson()

    fun <T> doMap(mapper: ((String)->T)): T{
        val str = gson.toJson(
            listOf(
                listOf(
                    User(
                        1, 1, "123"
                    )
                )
            )
        )
        return mapper(str)
    }

    inline fun <reified T> map(): T{
        val mapper = { str: String->
            gson.fromJson<T>(str)
        }
        return doMap(mapper)
    }

    @Test
    fun test() {
        val users = map<List<List<User>>>()
        Assert.assertEquals(users[0]::class, ArrayList::class)
        Assert.assertEquals(users[0][0]::class, User::class)
        val te = object : AbstractApi<List<List<User>>>(){
            override fun request(): Request {
                TODO("Not yet implemented")
            }
        }
        te.testCall()
    }
}