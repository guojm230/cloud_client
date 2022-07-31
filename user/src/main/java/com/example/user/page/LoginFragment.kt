package com.example.user.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.user.R
import com.example.user.databinding.FragmentLoginBinding
import com.example.user.notifyVerifyCode
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

    private val loginTypeTab get() = binding.loginTab

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
                    notifyVerifyCode(requireContext(), result.code)
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

        loginTypeTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (loginTypeTab.getTabAt(0) == tab) {
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


    private fun showAlert(str: String) {
        Snackbar.make(loginBtn, str, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}