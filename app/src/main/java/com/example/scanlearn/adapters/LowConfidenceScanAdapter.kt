package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemLowConfidenceScanBinding
import com.example.scanlearn.models.LowConfidenceScanInsight
import java.text.SimpleDateFormat
import java.util.Locale

class LowConfidenceScanAdapter(
    private val items: List<LowConfidenceScanInsight>
) : RecyclerView.Adapter<LowConfidenceScanAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLowConfidenceScanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLowConfidenceScanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvObjectName.text = item.objectName
        holder.binding.tvCategory.text = item.category.replaceFirstChar { it.uppercase() }
        holder.binding.tvConfidence.text = "${item.confidencePercent}%"
        holder.binding.tvSource.text = if (item.manualCorrection) "Teacher review: manually corrected" else "Teacher review: chosen from suggestions"

        try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val parsed = input.parse(item.createdAt)
            val output = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            holder.binding.tvDate.text = if (parsed != null) output.format(parsed) else item.createdAt
        } catch (_: Exception) {
            holder.binding.tvDate.text = item.createdAt
        }
    }

    override fun getItemCount() = items.size
}
