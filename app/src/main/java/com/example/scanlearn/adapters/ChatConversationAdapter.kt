package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemChatConversationBinding
import com.example.scanlearn.models.ChatConversation

class ChatConversationAdapter(
    private val currentUserId: String,
    private val onClick: (ChatConversation) -> Unit
) : RecyclerView.Adapter<ChatConversationAdapter.ViewHolder>() {

    private val items = mutableListOf<ChatConversation>()

    inner class ViewHolder(val binding: ItemChatConversationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = items[position]
        val partnerId = conversation.participantIds.firstOrNull { it != currentUserId }.orEmpty()
        val partnerName = conversation.participantNames[partnerId].orEmpty().ifBlank { "Chat" }
        val partnerRole = conversation.participantRoles[partnerId].orEmpty()

        with(holder.binding) {
            tvConversationName.text = partnerName
            tvConversationRole.text = partnerRole.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }.ifBlank { "User" }
            tvConversationPreview.text = conversation.lastMessage.ifBlank { "Start a conversation" }
            tvConversationTime.text = conversation.lastUpdatedAt.take(16).replace('T', ' ')
            val unreadCount = conversation.unreadCounts[currentUserId] ?: 0
            chipUnreadCount.text = unreadCount.toString()
            chipUnreadCount.visibility = if (unreadCount > 0) android.view.View.VISIBLE else android.view.View.GONE
            root.setOnClickListener { onClick(conversation) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(conversations: List<ChatConversation>) {
        items.clear()
        items.addAll(conversations)
        notifyDataSetChanged()
    }
}
