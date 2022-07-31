package com.example.repository

import com.example.repository.api.model.User
import com.google.gson.Gson
import org.junit.Assert
import org.junit.Test

class JsonTest {
    @Test
    fun test() {
        val gson = Gson()
        val str = gson.toJson(
            listOf(
                listOf(
                    User(
                        1, 1, "123"
                    )
                )
            )
        )
        val users = com.example.repository.gson.fromJson<List<List<User>>>(str)
        Assert.assertEquals(users[0]::class, ArrayList::class)
        Assert.assertEquals(users[0][0]::class, User::class)
    }
}