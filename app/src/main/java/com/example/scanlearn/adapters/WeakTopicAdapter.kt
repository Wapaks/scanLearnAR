package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemWeakTopicBinding
import com.example.scanlearn.models.WeakTopicInsight

class WeakTopicAdapter(
    private val items: List<WeakTopicInsight>
) : RecyclerView.Adapter<WeakTopicAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWeakTopicBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWeakTopicBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvObjectName.text = item.objectName
        holder.binding.tvAttempts.text = "${item.attemptsCount} attempt(s)"
        holder.binding.tvAverage.text = "${item.averageScorePercent}%"
    }

    override fun getItemCount() = items.size
}
