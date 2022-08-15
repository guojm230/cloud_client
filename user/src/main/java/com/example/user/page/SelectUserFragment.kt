package com.example.user.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.base.nav.clearAndNavigate
import com.example.base.nav.deeplink.ACTION_BACK
import com.example.base.nav.deeplink.ACTION_DEFAULT
import com.example.base.nav.deeplink.MainDeepLink
import com.example.user.R
import com.example.user.components.SelectUserListAdapter
import com.example.user.databinding.FragmentSelectUserBinding
import com.example.user.vm.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectUserFragment : Fragment() {

    private var _binding: FragmentSelectUserBinding? = null

    private val binding: FragmentSelectUserBinding get() = _binding!!

    private val loginViewModel by viewModels<LoginViewModel>({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectUserBinding.inflate(inflater)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        loginViewModel.loadUsers().observe(viewLifecycleOwner) {
            binding.userList.adapter = SelectUserListAdapter(requireContext(), it).apply {
                onSelectListener = { user ->
                    loginViewModel.selectUser(user)
                    enterNextScreen()
                }
            }
        }
        loginViewModel.loadUsers()
    }

    private fun initView() {
        binding.selectUserDescTextView.apply {
            val desc = requireContext().getString(R.string.select_user_desc)
            text = desc.replace("{tel}", loginViewModel.currentAccount()!!.tel)
        }
        binding.userList.layoutManager = LinearLayoutManager(context)
    }

    private fun enterNextScreen() {
        val action = arguments?.getInt("action", ACTION_DEFAULT)
        if (action == ACTION_BACK) {
            findNavController().popBackStack()
        } else {
            findNavController().clearAndNavigate(MainDeepLink)
        }
    }

}