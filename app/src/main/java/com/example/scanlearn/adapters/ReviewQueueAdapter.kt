package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemReviewQueueBinding

class ReviewQueueAdapter(
    private val items: List<ReviewQueueItem>,
    private val onAction: (ReviewQueueItem) -> Unit
) : RecyclerView.Adapter<ReviewQueueAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewQueueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemReviewQueueBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReviewQueueItem) {
            binding.tvReviewTag.text = item.tag
            binding.tvReviewTitle.text = item.title
            binding.tvReviewSubtitle.text = item.subtitle
            binding.btnReviewAction.text = item.actionLabel
            binding.btnReviewAction.setOnClickListener { onAction(item) }
            binding.root.setOnClickListener { onAction(item) }
        }
    }

    data class ReviewQueueItem(
        val tag: String,
        val title: String,
        val subtitle: String,
        val actionLabel: String,
        val targetType: String,
        val targetId: String
    )
}
