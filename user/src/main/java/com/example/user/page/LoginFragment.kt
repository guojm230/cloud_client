package com.example.user.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.repository.api.nextID
import com.example.user.R
import com.example.user.databinding.FragmentLoginBinding
import com.example.user.vm.LoginViewModel
import com.example.user.vm.consume
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    private val loginInput get() = binding.loginInput

    private val loginBtn get() = binding.loginBtn

    private val loginTab get() = binding.loginTab

    private val viewModel by viewModels<LoginViewModel>({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        initEvent()
        return binding.root
    }

    private fun initEvent() {
        viewModel.requireCodeResult.consume(viewLifecycleOwner) { result ->
            if (result.success) {
                if (result.notify) {
                    notifyVerifyCode(result.code)
                }
                findNavController().navigate(R.id.action_LoginFragment_to_verifyCodeFragment)
            } else {
                showAlert(result.errorMsg)
            }
        }

        viewModel.canLogin.observe(viewLifecycleOwner) {
            this.loginBtn.isEnabled = it
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        loginTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (loginTab.getTabAt(0) == tab) {
                    loginInput.switchToTelLogin()
                } else {
                    loginInput.switchToEmailLogin()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        loginInput.observe(viewModel.loginData)

        loginBtn.setOnClickListener {
            viewModel.login()
        }

    }

    private fun notifyVerifyCode(code: String) {
        val channelId = requireContext().getString(R.string.notification_channel_id)
        val text = requireContext().getString(R.string.notification_verify_code_template).run {
            replace("\${code}", code)
        }
        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setContentTitle("验证码")
            .setContentText(text)
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(requireContext()).notify(nextID(), notification)
    }

    private fun showAlert(str: String) {
        Snackbar.make(loginBtn, str, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}