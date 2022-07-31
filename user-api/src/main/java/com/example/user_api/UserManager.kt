package com.example.user_api

import androidx.lifecycle.LiveData
import com.example.repository.api.model.Account
import com.example.repository.api.model.User

interface UserManager {

    fun currentAccount(): LiveData<Account>

    fun currentUser(): LiveData<User>

    fun switchAccount()

    fun switchUser()

    fun logout()

}