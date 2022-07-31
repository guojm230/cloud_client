package com.example.user.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.api.UserRepository
import com.example.repository.api.model.Account
import com.example.repository.api.model.User
import com.example.user.components.LoginType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


data class RequireCodeResult(
    val success: Boolean,
    val code: String = "",
    val notify: Boolean = true,
    val errorMsg: String = ""
)

data class VerifyCodeResult(
    val success: Boolean, val errorMsg: String = ""
)

data class LoginData(
    val loginType: LoginType, val username: String, val password: String? = null
)

val telRegex = Regex("^\\d{11}$")
val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")

/**
 * 负责整个登录流程的ViewModel，多个Fragment共享，应该与Activity生命周期绑定，由登录成功后手动清除
 */
@HiltViewModel
class LoginViewModel @Inject constructor(val userRepository: UserRepository) : ViewModel() {

    private val _requireCodeResult = MutableLiveData<SingleEvent<RequireCodeResult>>()
    val requireCodeResult: LiveData<SingleEvent<RequireCodeResult>> = _requireCodeResult

    private val _verifyCodeResult = MutableLiveData<SingleEvent<VerifyCodeResult>>()
    val verifyCodeResult: LiveData<SingleEvent<VerifyCodeResult>> = _verifyCodeResult

    private val _account = MutableLiveData<Account>()
    val account: LiveData<Account> = _account

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _canLogin = MutableLiveData(false)
    val canLogin: LiveData<Boolean> = _canLogin

    //可重新获取验证码倒计时
    val retryTime: MutableLiveData<Int> = MutableLiveData(0)

    val loginData: MutableLiveData<LoginData> = MutableLiveData()

    init {
        loginData.observeForever {
            _canLogin.value = when (it.loginType) {
                LoginType.TEL_LOGIN -> validateTel(it.username)
                LoginType.EMAIL_LOGIN -> validateEmail(it.username)
                else -> false
            }
        }
    }

    fun login() {
        viewModelScope.launch {
            val (loginType, username) = loginData.value!!
            if ((retryTime.value ?: 0) > 0) {    //不需要重新获取，直接跳转
                val prevResult = _requireCodeResult.value!!.data
                val result = RequireCodeResult(true, prevResult.code, notify = false)
                _requireCodeResult.postValue(SingleEvent(result))
                return@launch
            }
            val codeResult = userRepository.requireVerifyCode(username, loginType.code)
            if (codeResult.isSuccess) {
                _requireCodeResult.postValue(
                    SingleEvent(
                        RequireCodeResult(
                            true, codeResult.data!!
                        )
                    )
                ) //开始定时
                launch {
                    retryTime.postValue(60)
                    repeat(60) {
                        delay(1000)
                        retryTime.postValue(retryTime.value?.dec())
                    }
                }
            } else {
                val errorResult = RequireCodeResult(false, errorMsg = "用户不存在")
                _requireCodeResult.postEvent(errorResult)
            }
        }
    }


    fun verifyCode(code: String) {
        viewModelScope.launch {
            val username = loginData.value!!.username
            val accountResult = userRepository.verifyCode(username, code)
            if (accountResult.isSuccess) {
                userRepository.setCurrentAccount(accountResult.data!!)
                _account.postValue(accountResult.data!!)
                _verifyCodeResult.postEvent(VerifyCodeResult(true))
            } else {
                _verifyCodeResult.postEvent(VerifyCodeResult(false, "验证码验证失败"))
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            val result = userRepository.queryUsers()
            if (result.isSuccess) {
                _users.postValue(result.data!!)
            }
        }
    }

    fun selectUser(user: User) {
        viewModelScope.launch {
            userRepository.setCurrentUser(user)
        }
    }


    private fun validateTel(text: String): Boolean {
        return telRegex.matches(text)
    }

    private fun validateEmail(text: String): Boolean {
        return emailRegex.matches(text)
    }
}