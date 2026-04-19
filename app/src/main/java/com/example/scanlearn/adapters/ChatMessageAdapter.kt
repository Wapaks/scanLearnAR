package com.example.scanlearn.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.R
import com.example.scanlearn.databinding.ItemChatMessageBinding
import com.example.scanlearn.models.ChatMessage

class ChatMessageAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {

    private val items = mutableListOf<ChatMessage>()

    inner class ViewHolder(val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = items[position]
        val isMine = message.senderId == currentUserId
        val context = holder.binding.root.context

        with(holder.binding) {
            (bubbleContainer.layoutParams as ViewGroup.MarginLayoutParams).let { params ->
                if (isMine) {
                    params.marginStart = context.resources.getDimensionPixelSize(R.dimen.chat_message_margin_large)
                    params.marginEnd = 0
                } else {
                    params.marginStart = 0
                    params.marginEnd = context.resources.getDimensionPixelSize(R.dimen.chat_message_margin_large)
                }
                bubbleContainer.layoutParams = params
            }

            bubbleContainer.gravity = if (isMine) Gravity.END else Gravity.START
            messageCard.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (isMine) R.color.primary else R.color.surface
                )
            )
            tvMessage.text = message.message
            tvMessage.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isMine) android.R.color.white else R.color.text_primary
                )
            )
            tvTimestamp.text = message.createdAt.take(16).replace('T', ' ')
            tvTimestamp.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isMine) android.R.color.white else R.color.text_secondary
                )
            )
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(messages: List<ChatMessage>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }
}
