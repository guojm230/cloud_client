package com.example.user.page

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.example.base.event.consume
import com.example.base.util.nextID
import com.example.user.R
import com.example.user.databinding.FragmentVerifyCodeBinding
import com.example.user.vm.LoginViewModel
import com.google.android.material.snackbar.Snackbar


class VerifyCodeFragment : Fragment() {

    private var _binding: FragmentVerifyCodeBinding? = null
    private val binding get() = _binding!!

    private val retryBtn get() = binding.verifyCodeRetryBtn
    private val verifyCodeInput get() = binding.verifyCodeInput
    private val retryTimeTextView get() = binding.verifyCodeTime
    private val retryTimeLayout get() = binding.verifyCodeTimeLayout

    private val viewModel by viewModels<LoginViewModel>({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        enterTransition =
            TransitionInflater.from(context!!).inflateTransition(R.transition.transition_bottom)
        _binding = FragmentVerifyCodeBinding.inflate(inflater, container, false)

        val codeDesc = getString(R.string.verify_code_desc)
        val username = viewModel.loginData.value!!.username
        binding.verifyCodeDesc.text = codeDesc.replace("{tel}", username)

        initEvent()
        return binding.root
    }

    private fun initEvent() {
        viewModel.verifyCodeResult.consume(viewLifecycleOwner) {
            if (it.success) {
                val options = NavOptions.Builder().run {
                    setPopUpTo(R.id.verifyCodeFragment, true)
                    build()
                }
                findNavController().navigate(
                    R.id.action_verifyCodeFragment_to_selectUserFragment, null, options
                )
            } else {
                verifyCodeInput.reset()
                showAlert(it.errorMsg)
            }
        }

        //倒计时文本
        viewModel.retryTime.observe(viewLifecycleOwner) {
            retryTimeTextView.text = it.toString()
            if (it <= 0) {
                retryBtn.visibility = View.VISIBLE
                retryTimeLayout.visibility = View.INVISIBLE
            } else {
                retryBtn.visibility = View.INVISIBLE
                retryTimeLayout.visibility = View.VISIBLE
            }
        }

        retryBtn.setOnClickListener { //重新获取code
            viewModel.login().consume(viewLifecycleOwner) {
                if (it.success && it.notify) {
                    viewModel.notifyVerifyCode(requireContext(), it.code)
                } else {
                    showAlert(it.errorMsg)
                }
            }
        }

        verifyCodeInput.onCompleteListener = { verifyCodeInput, s ->
            viewModel.verifyCode(s)
        }
    }

    private fun showAlert(str: String) {
        Snackbar.make(verifyCodeInput, str, Snackbar.LENGTH_LONG).show()
    }
}