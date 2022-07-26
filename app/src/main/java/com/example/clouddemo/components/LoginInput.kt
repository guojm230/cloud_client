package com.example.clouddemo.components

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.clouddemo.R
import com.example.clouddemo.databinding.LoginInputLayoutBinding
import com.google.android.material.textfield.TextInputEditText

const val TEL_LOGIN = 1
const val EMAIL_LOGIN = 2

/**
 * 支持手机和Email两种样式的输入框组件
 */
class LoginInput(context: Context,attrs:AttributeSet? = null,
                 defStyleAttr: Int = 0,defStyleRes: Int = 0) : FrameLayout(context,attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context,attrs: AttributeSet?): this(context,attrs,0,0)

    private val binding: LoginInputLayoutBinding
    private val telInputLayout: ViewGroup
    private val emailInputView: TextInputEditText

    var loginType = TEL_LOGIN
        private set

    val input: String
        get() = if (loginType == TEL_LOGIN) binding.telInput.text.toString()
            else emailInputView.text.toString()

    init {
        background = ContextCompat.getDrawable(context,R.drawable.login_input_border)
        binding = LoginInputLayoutBinding.inflate(LayoutInflater.from(context),this)
        telInputLayout = binding.telInputLayout
        emailInputView = binding.emailLoginInput
    }


    fun switchToTelLogin(){
        if(loginType != TEL_LOGIN){
            telInputLayout.visibility = VISIBLE
            emailInputView.visibility = INVISIBLE
            loginType = TEL_LOGIN
        }
    }

    fun switchToEmailLogin(){
        if(loginType != EMAIL_LOGIN){
            telInputLayout.visibility = INVISIBLE
            emailInputView.visibility = VISIBLE
            loginType = EMAIL_LOGIN
        }
        requestLayout()
    }

}