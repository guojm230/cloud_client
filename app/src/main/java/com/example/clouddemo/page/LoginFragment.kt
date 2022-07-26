package com.example.clouddemo.page

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.clouddemo.R
import com.example.clouddemo.components.LoginInput
import com.example.clouddemo.databinding.FragmentLoginBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var loginInput: LoginInput

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.loginTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(binding.loginTab.getTabAt(0) == tab){
                    binding.loginInput.switchToTelLogin()
                } else {
                    binding.loginInput.switchToEmailLogin()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        binding.loginBtn.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_verifyCodeFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}