package com.example.cloud.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.api.UserRepository
import com.example.repository.api.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val currentUser = userRepository.liveCurrentUser()
    val currentAccount = userRepository.liveCurrentAccount()

    private val _users = MutableLiveData<List<User>>()
    val currentUsers: LiveData<List<User>> = _users


    fun loadUsers(): LiveData<List<User>> {
        viewModelScope.launch {
            val result = userRepository.queryUsers()
            if (result.isSuccess) {
                _users.postValue(result.data)
            } else {
                println(result.toString())
            }
        }
        return currentUsers
    }

    fun switchUser(userId: Int) {
        viewModelScope.launch {
            if (_users.value == null) {
                val userResult = userRepository.queryUsers()
                if (userResult.isSuccess) {
                    val toUser = userResult.data!!.find { it.id == userId }
                    userRepository.setCurrentUser(toUser!!)
                }
            } else {
                val toUser = _users.value!!.find { it.id == userId }
                userRepository.setCurrentUser(toUser!!)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout(currentAccount.value!!)
        }
    }

}