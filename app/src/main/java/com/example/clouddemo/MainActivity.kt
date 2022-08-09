package com.example.clouddemo

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.base.deeplink.MainDeepLink
import com.example.base.deeplink.SelectUserDeepLink
import com.example.base.deeplink.WelcomeDeepLink
import com.example.base.result.GlobalErrorHandler
import com.example.base.result.ErrorCode
import com.example.clouddemo.databinding.ActivityMainBinding
import com.example.user.vm.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private val loginViewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            if (loginViewModel.isAuthenticated()) {
                val nav =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
                val targetLink = if (loginViewModel.currentUser() == null) {    //登录但没选择角色
                    SelectUserDeepLink
                } else {
                    MainDeepLink
                } //清空路由回退栈
                nav.navController.navigate(
                    targetLink,
                    NavOptions.Builder().setPopUpTo(com.example.user.R.id.welcomeFragment, true)
                        .build()
                )
            } //没有登录时进入默认路由即可
        }
        initErrorHandler()
        setContentView(binding.root)
    }

    private fun initErrorHandler(){
        GlobalErrorHandler.addLastHandler {
            when(it.code){
                //token过期，重新登录
                ErrorCode.INVALID_TOKEN -> {
                    findNavController(R.id.nav_host_fragment_content_main).navigate(
                        WelcomeDeepLink,
                        NavOptions.Builder().run {
                            setPopUpTo(com.example.cloud.R.id.mainFragment,true)
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

    private fun showSnackBar(text: String){
        Snackbar.make(window.decorView,text,Snackbar.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }



}