package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemCategoryInsightBinding
import com.example.scanlearn.models.CategoryAnalytics

class CategoryInsightAdapter(
    private val items: List<CategoryAnalytics>
) : RecyclerView.Adapter<CategoryInsightAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCategoryInsightBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryInsightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvCategory.text = item.category.replaceFirstChar { it.uppercase() }
        holder.binding.tvSummary.text =
            "${item.manualCorrections} manual fixes - ${item.lowConfidenceSelections} low-confidence - ${item.averageQuizScorePercent}% avg"
        holder.binding.tvVolume.text = "${item.totalSelections} total selections"
    }

    override fun getItemCount(): Int = items.size
}
