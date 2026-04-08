package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemQuizAttemptBinding
import com.example.scanlearn.models.QuizAttempt
import java.text.SimpleDateFormat
import java.util.Locale

class QuizAttemptAdapter(
    private val attempts: List<QuizAttempt>
) : RecyclerView.Adapter<QuizAttemptAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemQuizAttemptBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuizAttemptBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attempt = attempts[position]
        holder.binding.tvObjectName.text = attempt.objectName
        holder.binding.tvScore.text = "${attempt.score}/${attempt.totalQuestions}"
        val percent = if (attempt.totalQuestions == 0) 0 else (attempt.score * 100) / attempt.totalQuestions
        holder.binding.tvPercent.text = "$percent%"
        holder.binding.tvMode.text = attempt.mode.replaceFirstChar { it.uppercase() }

        try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val parsed = input.parse(attempt.completedAt)
            val output = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            holder.binding.tvDate.text = if (parsed != null) output.format(parsed) else attempt.completedAt
        } catch (_: Exception) {
            holder.binding.tvDate.text = attempt.completedAt
        }
    }

    override fun getItemCount() = attempts.size
}
