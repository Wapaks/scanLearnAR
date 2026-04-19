package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemLessonOutlineBinding
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.StudentLessonProgress

class LessonOutlineAdapter(
    private val lessons: List<Lesson>,
    private val progressMap: Map<String, StudentLessonProgress>,
    private val actionLabel: String,
    private val onLessonClick: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonOutlineAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLessonOutlineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lessons[position])
    }

    override fun getItemCount(): Int = lessons.size

    inner class ViewHolder(
        private val binding: ItemLessonOutlineBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            val progress = progressMap[lesson.id]
            val isCompleted = progress?.status == "completed"
            val mastery = progress?.masteryStatus?.replace("_", " ")?.replaceFirstChar { it.uppercase() }
                ?: "Not started"

            binding.tvLessonTitle.text = lesson.title
            binding.tvLessonObjective.text = lesson.objective
            binding.tvLessonMeta.text = "${lesson.estimatedMinutes} min • $mastery"
            binding.tvLessonBadge.text = if (isCompleted) "Done" else "Up Next"
            binding.btnLessonAction.text = if (isCompleted) "Review" else actionLabel
            binding.btnLessonAction.setOnClickListener { onLessonClick(lesson) }
            binding.root.setOnClickListener { onLessonClick(lesson) }
        }
    }
}
