package com.example.clouddemo.page

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.clouddemo.R
import com.example.clouddemo.databinding.FragmentLoginBinding
import com.example.clouddemo.databinding.FragmentSelectUserBinding
import com.example.repository.api.model.User
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class SelectUserFragment : Fragment() {

    private lateinit var binding: FragmentSelectUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectUserBinding.inflate(inflater)
        val users = listOf<User>(User(1,"guojm"),User(2,"test"))
        binding.userList.layoutManager = LinearLayoutManager(context)
        val adapter = SelectUserListAdapter(context!!,users).apply {
            listener = {

            }
        }
        binding.userList.adapter = adapter
        return binding.root
    }

    class SelectUserListAdapter(val context: Context,val users: List<User>): RecyclerView.Adapter<UserListItemViewHolder>() {

        public var listener: (()->Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListItemViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.select_user_list_item,parent,false)
            return UserListItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserListItemViewHolder, position: Int) {
            (holder.itemView as ViewGroup).findViewById<MaterialTextView>(R.id.name_text_view)
                .apply {
                    text = users[position].name
                }
            (holder.itemView as MaterialCardView).setOnClickListener {
                listener?.invoke()
            }
        }

        override fun getItemCount(): Int {
            return users.size
        }

    }

    class UserListItemViewHolder(view: View): RecyclerView.ViewHolder(view){

    }

}