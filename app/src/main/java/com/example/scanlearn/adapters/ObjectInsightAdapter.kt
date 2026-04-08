package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemObjectInsightBinding
import com.example.scanlearn.models.LearningObjectAnalytics

class ObjectInsightAdapter(
    private val items: List<LearningObjectAnalytics>,
    private val metricBuilder: (LearningObjectAnalytics) -> String,
    private val onClick: (LearningObjectAnalytics) -> Unit
) : RecyclerView.Adapter<ObjectInsightAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemObjectInsightBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemObjectInsightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.objectName
        holder.binding.tvCategory.text = item.category.replaceFirstChar { it.uppercase() }
        holder.binding.tvMetric.text = metricBuilder(item)
        holder.binding.root.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
