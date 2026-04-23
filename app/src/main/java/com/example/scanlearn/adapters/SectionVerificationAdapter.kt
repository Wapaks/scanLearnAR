package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemSectionVerificationBinding

class SectionVerificationAdapter(
    private val items: List<SectionVerificationRow>
) : RecyclerView.Adapter<SectionVerificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSectionVerificationBinding.inflate(
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
        private val binding: ItemSectionVerificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SectionVerificationRow) {
            binding.tvSectionName.text = item.sectionName
            binding.tvSectionMeta.text = "${item.studentCount} student(s) assigned"
            binding.tvSectionStatus.text = when {
                item.hasInvalidAssignments -> "Check Assignments"
                item.studentCount == 0 -> "No Students Yet"
                else -> "Ready"
            }
            binding.tvSectionHint.text = when {
                item.hasInvalidAssignments ->
                    "Some learners in this grade have missing or invalid section assignments."
                item.studentCount == 0 ->
                    "This master section exists in Firebase but has no learners assigned yet."
                else ->
                    "Student assignments match the section master data for this grade."
            }
        }
    }

    data class SectionVerificationRow(
        val sectionName: String,
        val studentCount: Int,
        val hasInvalidAssignments: Boolean
    )
}
