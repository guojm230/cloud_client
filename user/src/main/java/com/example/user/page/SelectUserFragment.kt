package com.example.user.page

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base.deeplink.MainDeepLink
import com.example.repository.api.model.User
import com.example.user.R
import com.example.user.databinding.FragmentSelectUserBinding
import com.example.user.vm.LoginViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectUserFragment : Fragment() {

    private lateinit var binding: FragmentSelectUserBinding

    private val viewModel by viewModels<LoginViewModel>({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectUserBinding.inflate(inflater)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.loadUsers().observe(viewLifecycleOwner) {
            binding.userList.adapter = SelectUserListAdapter(requireContext(), it).apply {
                listener = { user->
                    viewModel.selectUser(user)
                    enterSelectUser()
                }
            }
        }
        binding.selectUserDescTextView.apply {
            val desc = requireContext().getString(R.string.select_user_desc)
            text = desc.replace("{tel}", viewModel.currentAccount()!!.tel)
        }
        binding.userList.layoutManager = LinearLayoutManager(context)

        viewModel.loadUsers()
        return binding.root
    }

    class SelectUserListAdapter(val context: Context, val users: List<User>) :
        RecyclerView.Adapter<UserListItemViewHolder>() {

        var listener: ((User) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListItemViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.select_user_list_item, parent, false)
            return UserListItemViewHolder(view as MaterialCardView)
        }

        override fun onBindViewHolder(holder: UserListItemViewHolder, position: Int) {
            holder.itemView.findViewById<MaterialTextView>(R.id.name_text_view)
                .apply {
                    text = users[position].name
                }
            holder.itemView.findViewById<AppCompatButton>(R.id.select_user_btn)
                .setOnClickListener {
                    listener?.invoke(users[position])
                }
        }

        override fun getItemCount(): Int {
            return users.size
        }

    }

    private fun enterSelectUser() {
        findNavController().navigate(
            MainDeepLink, NavOptions.Builder().setPopUpTo(R.id.welcomeFragment, true).build()
        )
    }

    class UserListItemViewHolder(view: MaterialCardView) : RecyclerView.ViewHolder(view)

}