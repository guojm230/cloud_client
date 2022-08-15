package com.example.user.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.base.event.SingleEvent
import com.example.base.event.postEvent
import com.example.base.result.ErrorCode
import com.example.base.result.catchError
import com.example.base.result.onSuccess
import com.example.base.result.runPostError
import com.example.base.util.emailRegex
import com.example.base.util.telRegex
import com.example.repository.api.UserRepository
import com.example.repository.api.model.Account
import com.example.repository.api.model.User
import com.example.user.components.LoginType
import com.example.user.model.LoginData
import com.example.user.model.RequireCodeResult
import com.example.user.model.VerifyCodeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * 负责整个登录流程的ViewModel，多个Fragment共享，应该与Activity生命周期绑定
 */
@HiltViewModel
class LoginViewModel @Inject constructor(val userRepository: UserRepository) : ViewModel() {

    private val _requireCodeResult = MutableLiveData<SingleEvent<RequireCodeResult>>()
    val requireCodeResult: LiveData<SingleEvent<RequireCodeResult>> = _requireCodeResult

    private val _verifyCodeResult = MutableLiveData<SingleEvent<VerifyCodeResult>>()
    val verifyCodeResult: LiveData<SingleEvent<VerifyCodeResult>> = _verifyCodeResult

    private val _users = MutableLiveData<List<User>>()

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

    fun requireVerifyCode(): LiveData<SingleEvent<RequireCodeResult>> {
        viewModelScope.launch {
            if (shouldSkipRequireCode()) {    //不需要重新获取验证码，直接跳转
                return@launch
            }
            val (loginType, username) = loginData.value!!
            val codeResult = userRepository.requireVerifyCode(username, loginType.code)
            codeResult.onSuccess { code ->
                _requireCodeResult.postValue(
                    SingleEvent(
                        RequireCodeResult(
                            true, code
                        )
                    )
                ) //开始倒计时
                launch {
                    retryTime.postValue(60)
                    repeat(60) {
                        delay(1000)
                        retryTime.postValue(retryTime.value?.dec())
                    }
                }
            }.catchError(ErrorCode.NOT_FOUND) {
                val errorResult = RequireCodeResult(false, errorMsg = "用户不存在")
                _requireCodeResult.postEvent(errorResult)
            }
        }
        return requireCodeResult
    }

    private fun shouldSkipRequireCode(): Boolean {
        if ((retryTime.value ?: 0) > 0) {    //不需要重新获取验证码，直接跳转
            val prevResult = _requireCodeResult.value!!.data
            val result = RequireCodeResult(true, prevResult.code, notify = false)
            _requireCodeResult.postValue(SingleEvent(result))
            return true
        }
        return false
    }

    fun verifyCode(code: String) {
        viewModelScope.launch {
            val username = loginData.value!!.username
            userRepository.verifyCode(username, code)
                .onSuccess {
                    userRepository.setCurrentAccount(it)
                    _verifyCodeResult.postEvent(VerifyCodeResult(true))
                }.catchError(ErrorCode.VERIFY_CODE_ERROR) {
                    _verifyCodeResult.postEvent(VerifyCodeResult(false, "验证码错误"))
                }
        }
    }

    fun loadUsers(): LiveData<List<User>> {
        viewModelScope.launch {
            val result = userRepository.queryUsers()
            result.runPostError {
                _users.postValue(it)
            }
        }
        return _users
    }

    fun selectUser(user: User) {
        viewModelScope.launch {
            userRepository.setCurrentUser(user)
        }
    }

    fun currentAccount(): Account? {
        return userRepository.currentAccount()
    }

    private fun validateTel(text: String): Boolean {
        return telRegex.matches(text)
    }

    private fun validateEmail(text: String): Boolean {
        return emailRegex.matches(text)
    }

}