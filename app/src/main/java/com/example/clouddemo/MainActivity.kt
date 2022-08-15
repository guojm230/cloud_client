package com.example.clouddemo

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.base.AppContext
import com.example.base.nav.clearAndNavigate
import com.example.base.nav.deeplink.MainDeepLink
import com.example.base.nav.deeplink.SelectUserDeepLink
import com.example.base.nav.deeplink.WelcomeDeepLink
import com.example.base.result.ErrorCode
import com.example.base.result.GlobalErrorHandler
import com.example.cloud.vm.UserViewModel
import com.example.clouddemo.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private val userViewModel by viewModels<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        AppContext.setActivityContext(this)
        initErrorHandler()

        if (userViewModel.isAuthenticated()) {
            val nav =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
            val targetLink = if (userViewModel.isSelectUser()) {    //登录但没选择角色
                MainDeepLink
            } else {
                SelectUserDeepLink
            }
            nav.navController.popBackStack()
            //清空路由回退栈
            nav.navController.clearAndNavigate(targetLink)
        } //没有登录时进入默认路由即可

        setContentView(binding.root)
    }

    private fun initErrorHandler() {
        //未捕获异常，主要打印日志记录，方便排查
        Thread.currentThread().setUncaughtExceptionHandler { t, e ->
            Log.e(TAG, "uncaughtExceptionHandler: 未捕获异常", e)
            showSnackBar("未知错误")
        }

        GlobalErrorHandler.addLastHandler {
            when (it.code) {
                //token过期，重新登录
                ErrorCode.INVALID_TOKEN -> {
                    findNavController(R.id.nav_host_fragment_content_main).navigate(
                        WelcomeDeepLink,
                        NavOptions.Builder().run {
                            setPopUpTo(com.example.cloud.R.id.mainFragment, true)
                            build()
                        }
                    )
                }
                else -> {
                    showSnackBar(it.msg)
                }
            }
            return@addLastHandler true
        }
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(window.decorView, text, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private val TAG = MainActivity::class.java.canonicalName
    }

}