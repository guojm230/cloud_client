package com.example.clouddemo.components

import android.content.Context
import android.graphics.Color
import android.provider.CalendarContract.Colors
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import com.example.clouddemo.R
import com.example.clouddemo.databinding.EmailInputLayoutBinding
import com.example.clouddemo.databinding.TelInputLayoutBinding
import com.google.android.material.textfield.TextInputEditText

const val TEL_LOGIN = 1
const val EMAIL_LOGIN = 2

/**
 * 支持手机和Email两种样式的输入框组件
 */
class LoginInput(context: Context,attrs:AttributeSet? = null,
                 defStyleAttr: Int = 0,defStyleRes: Int = 0) : FrameLayout(context,attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context,attrs: AttributeSet?): this(context,attrs,0,0)

    private val telInputBinding: TelInputLayoutBinding
    private val telInputView: ViewGroup
    private val emailInputBinding: EmailInputLayoutBinding
    private var emailInputView: View

    var loginType = TEL_LOGIN
        private set

    init {
        background = ContextCompat.getDrawable(context,R.drawable.login_input_border)
        val pt = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10F,context.resources.displayMetrics).toInt()
        val ps = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,12F,context.resources.displayMetrics).toInt()
        setPadding(ps,pt,ps,pt)

        telInputBinding = TelInputLayoutBinding.inflate(LayoutInflater.from(context),this,true)
        telInputView = telInputBinding.root
val tp = telInputView.layoutParams
        val fl = FrameLayout(context)
        fl.layoutParams = LayoutParams(-1,-1)
        emailInputView = fl
        emailInputBinding = EmailInputLayoutBinding.inflate(LayoutInflater.from(context),null,false)
        emailInputView = emailInputBinding.root
    }

    fun switchToTelLogin(){
        if(loginType != TEL_LOGIN){
            removeAllViews()
            addView(telInputView)
            loginType = TEL_LOGIN
        }
    }

    fun switchToEmailLogin(){
        if(loginType != EMAIL_LOGIN){
            removeAllViews()
            addView(emailInputView,LayoutParams(-1,-1))
            loginType = EMAIL_LOGIN
        }
        requestLayout()
    }



}