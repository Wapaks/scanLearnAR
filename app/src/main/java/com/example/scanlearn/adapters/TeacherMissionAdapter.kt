package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemTeacherMissionBinding
import com.example.scanlearn.models.Mission

class TeacherMissionAdapter(
    private val missions: List<Mission>,
    private val onEdit: (Mission) -> Unit,
    private val onToggleStatus: (Mission) -> Unit
) : RecyclerView.Adapter<TeacherMissionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTeacherMissionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeacherMissionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mission = missions[position]
        holder.binding.tvTitle.text = mission.title
        holder.binding.tvDescription.text = mission.description
        holder.binding.tvCategory.text = mission.category.replaceFirstChar { it.uppercase() }
        holder.binding.tvCurriculum.text = when {
            mission.lessonIds.isNotEmpty() ->
                "${formatQuarterLabel(mission.quarterId)} | ${mission.lessonIds.size} lesson link(s)"
            mission.quarterId.isNotBlank() -> formatQuarterLabel(mission.quarterId)
            else -> "No curriculum link yet"
        }
        val targetSections = if (mission.sectionIds.isEmpty()) {
            "All sections"
        } else {
            mission.sectionIds.joinToString(", ")
        }
        val releasedSections = if (mission.releasedSectionIds.isEmpty()) {
            "Not released yet"
        } else {
            mission.releasedSectionIds.joinToString(", ")
        }
        holder.binding.tvSections.text = "Targets: $targetSections | Released: $releasedSections"
        holder.binding.tvObjects.text = "${mission.objectsToFind.size} object(s) linked"
        holder.binding.tvStatus.text = if (mission.active) "Active" else "Archived"
        holder.binding.btnEdit.setOnClickListener { onEdit(mission) }
        holder.binding.btnToggleStatus.text = if (mission.active) "Archive" else "Reactivate"
        holder.binding.btnToggleStatus.setOnClickListener { onToggleStatus(mission) }
    }

    override fun getItemCount(): Int = missions.size

    private fun formatQuarterLabel(quarterId: String): String {
        val match = Regex("q(\\d+)", RegexOption.IGNORE_CASE).find(quarterId)
        return if (match != null) {
            "Quarter ${match.groupValues[1]}"
        } else {
            quarterId.ifBlank { "Quarter not linked" }
        }
    }
}
