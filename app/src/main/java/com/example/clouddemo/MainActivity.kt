package com.example.clouddemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.base.deeplink.MainDeepLink
import com.example.base.deeplink.SelectUserDeepLink
import com.example.clouddemo.databinding.ActivityMainBinding
import com.example.user.vm.LoginViewModel
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

        setContentView(binding.root)
    }




}