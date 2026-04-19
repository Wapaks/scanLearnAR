package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemChatContactBinding
import com.example.scanlearn.models.User

class ChatContactAdapter(
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<ChatContactAdapter.ViewHolder>() {

    private val items = mutableListOf<User>()

    inner class ViewHolder(val binding: ItemChatContactBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = items[position]
        with(holder.binding) {
            tvContactName.text = user.name
            tvContactRole.text = user.role.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
            tvContactMeta.text = user.section.ifBlank { user.email }
            root.setOnClickListener { onClick(user) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(users: List<User>) {
        items.clear()
        items.addAll(users)
        notifyDataSetChanged()
    }
}
