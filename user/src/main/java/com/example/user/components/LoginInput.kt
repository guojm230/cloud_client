package com.example.user.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import com.example.user.R
import com.example.user.databinding.LoginInputLayoutBinding
import com.example.user.model.LoginData
import com.google.android.material.textfield.TextInputEditText

enum class LoginType(val code: Int) {
    TEL_LOGIN(1),
    EMAIL_LOGIN(2),

    //暂不支持
    PASSWORD(3)
}

/**
 * 支持手机和Email两种样式的输入框组件
 */
class LoginInput(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)

    private val binding: LoginInputLayoutBinding
    private val telInputLayout: ViewGroup
    private val emailInputView: TextInputEditText

    private var changeListener: ((LoginType, String) -> Unit)? = null

    var loginType = LoginType.TEL_LOGIN
        private set

    val input: String
        get() = if (loginType == LoginType.TEL_LOGIN) binding.telInput.text?.toString() ?: ""
        else emailInputView.text?.toString() ?: ""

    val loginData: LoginData
        get() = LoginData(loginType, input)

    init {
        background = ContextCompat.getDrawable(context, R.drawable.login_input_border)
        binding = LoginInputLayoutBinding.inflate(LayoutInflater.from(context), this)
        telInputLayout = binding.telInputLayout
        emailInputView = binding.emailLoginInput

        binding.telInput.addTextChangedListener {
            this.changeListener?.invoke(loginType, input)
        }

        binding.emailLoginInput.addTextChangedListener {
            this.changeListener?.invoke(loginType, input)
        }
    }

    fun observe(loginData: MutableLiveData<LoginData>) {
        changeListener = { i, s ->
            loginData.value = loginData.value?.copy(loginType = i, username = s)
                ?: LoginData(i, s)
        }
    }

    fun switchToTelLogin() {
        if (loginType != LoginType.TEL_LOGIN) {
            telInputLayout.visibility = VISIBLE
            emailInputView.visibility = INVISIBLE
            loginType = LoginType.TEL_LOGIN
            changeListener?.invoke(loginType, input)
        }
    }

    fun switchToEmailLogin() {
        if (loginType != LoginType.EMAIL_LOGIN) {
            telInputLayout.visibility = INVISIBLE
            emailInputView.visibility = VISIBLE
            loginType = LoginType.EMAIL_LOGIN
            changeListener?.invoke(loginType, input)
        }
    }

}