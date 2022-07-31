package com.example.user.page

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectUserBinding.inflate(inflater)
        viewModel.users.observe(viewLifecycleOwner) {
            binding.userList.adapter = SelectUserListAdapter(context!!, it).apply {
                listener = {
                    enterSelectUser()
                }
            }
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
            return UserListItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserListItemViewHolder, position: Int) {
            (holder.itemView as ViewGroup).findViewById<MaterialTextView>(R.id.name_text_view)
                .apply {
                    text = users[position].name
                }
            (holder.itemView as MaterialCardView).findViewById<AppCompatButton>(R.id.select_user_btn)
                .setOnClickListener {
                    listener?.invoke(users[position])
                }
        }

        override fun getItemCount(): Int {
            return users.size
        }

    }

    private fun enterSelectUser() {
        val request =
            NavDeepLinkRequest.Builder.fromUri("android:app://com.example.cloud/file_list_fragment".toUri())
                .build()
        findNavController().navigate(
            request, NavOptions.Builder().setPopUpTo(R.id.welcomeFragment, true).build()
        )
    }

    class UserListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

}