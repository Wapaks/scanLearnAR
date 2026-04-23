package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemSectionIssueBinding

class SectionIssueAdapter(
    private val items: List<SectionIssueRow>,
    private val onRepair: (SectionIssueRow) -> Unit
) : RecyclerView.Adapter<SectionIssueAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSectionIssueBinding.inflate(
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
        private val binding: ItemSectionIssueBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SectionIssueRow) {
            binding.tvStudentName.text = item.studentName
            binding.tvStudentMeta.text = item.studentMeta
            binding.tvIssueDetail.text = item.issueDetail
            binding.btnRepair.setOnClickListener { onRepair(item) }
        }
    }

    data class SectionIssueRow(
        val userId: String,
        val studentName: String,
        val studentMeta: String,
        val issueDetail: String,
        val currentGradeLevel: String,
        val currentSection: String
    )
}
