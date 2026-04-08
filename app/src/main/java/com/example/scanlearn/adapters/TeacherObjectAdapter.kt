package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemTeacherObjectBinding
import com.example.scanlearn.models.LearningObjectAnalytics

class TeacherObjectAdapter(
    private val items: List<LearningObjectAnalytics>,
    private val onClick: (LearningObjectAnalytics) -> Unit
) : RecyclerView.Adapter<TeacherObjectAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTeacherObjectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeacherObjectBinding.inflate(
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
        holder.binding.tvStatus.text = item.status.replaceFirstChar { it.uppercase() }
        holder.binding.tvStatus.alpha = if (item.status.equals("archived", ignoreCase = true)) 0.65f else 1f
        holder.binding.tvSummary.text =
            "${item.totalScanSelections} scans • ${item.quizAttempts} quizzes • ${item.averageQuizScorePercent}% avg"
        holder.binding.tvSignals.text =
            "${item.lowConfidenceSelections} low-confidence • ${item.manualCorrections} manual fixes • ${item.recentLearners} learners"
        holder.binding.root.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
