package com.example.clouddemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.clouddemo.databinding.ActivityMainBinding
import com.example.user.vm.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity constructor() : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    val loginViewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.setDecorFitsSystemWindows(false)
        lifecycleScope.launch {
            if (loginViewModel.isAuthenticated()) {
                val nav =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
                nav.navController.navigate(
                    "android:app://com.example.cloud/file_list_fragment".toUri(),
                    NavOptions.Builder().setPopUpTo(com.example.user.R.id.welcomeFragment, true)
                        .build()
                )
            }
        }

        setContentView(binding.root)
    }


}