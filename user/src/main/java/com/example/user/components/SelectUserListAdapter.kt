package com.example.user.components

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.repository.api.model.User
import com.example.user.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class SelectUserListAdapter(val context: Context, val users: List<User>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onSelectListener: ((User) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.select_user_list_item, parent, false)
        return object : RecyclerView.ViewHolder(view as MaterialCardView) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.findViewById<MaterialTextView>(R.id.name_text_view)
            .apply {
                text = users[position].name
            }
        holder.itemView.findViewById<AppCompatButton>(R.id.select_user_btn)
            .setOnClickListener {
                onSelectListener?.invoke(users[position])
            }
    }

    override fun getItemCount(): Int {
        return users.size
    }

}