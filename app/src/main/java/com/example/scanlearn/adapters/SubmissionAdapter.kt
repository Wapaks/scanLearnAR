package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemSubmissionBinding
import com.example.scanlearn.models.Submission
import java.text.SimpleDateFormat
import java.util.*

class SubmissionAdapter(private val submissions: List<Submission>) :
    RecyclerView.Adapter<SubmissionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSubmissionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubmissionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sub = submissions[position]
        holder.binding.tvObjectName.text = sub.objectName
        holder.binding.tvScore.text = "${sub.quizScore}/${sub.totalQuestions}"
        holder.binding.tvLearnings.text = sub.learnings

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val date = sdf.parse(sub.timestamp)
            val display = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date!!)
            holder.binding.tvDate.text = display
        } catch (e: Exception) {
            holder.binding.tvDate.text = sub.timestamp
        }
    }

    override fun getItemCount() = submissions.size
}
