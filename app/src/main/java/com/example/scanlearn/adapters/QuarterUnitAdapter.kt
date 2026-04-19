package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemQuarterUnitBinding
import com.example.scanlearn.models.Unit as CurriculumUnit

class QuarterUnitAdapter(
    private val units: List<CurriculumUnit>,
    private val completedLessonCounts: Map<String, Int>,
    private val onUnitClick: (CurriculumUnit) -> kotlin.Unit
) : RecyclerView.Adapter<QuarterUnitAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuarterUnitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(units[position])
    }

    override fun getItemCount(): Int = units.size

    inner class ViewHolder(
        private val binding: ItemQuarterUnitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(unit: CurriculumUnit) {
            val totalLessons = unit.lessonIds.size.coerceAtLeast(1)
            val completedLessons = completedLessonCounts[unit.id] ?: 0
            val percent = (completedLessons * 100) / totalLessons

            binding.tvUnitTitle.text = unit.title
            binding.tvUnitOverview.text = unit.overview
            binding.tvUnitMeta.text = "$completedLessons of ${unit.lessonIds.size} lessons complete"
            binding.progressLessons.max = totalLessons
            binding.progressLessons.progress = completedLessons.coerceAtMost(totalLessons)
            binding.tvProgressPercent.text = "$percent%"
            binding.root.setOnClickListener { onUnitClick(unit) }
        }
    }
}
