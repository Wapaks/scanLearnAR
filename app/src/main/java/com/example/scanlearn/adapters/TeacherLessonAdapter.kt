package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemTeacherLessonBinding
import com.example.scanlearn.models.Lesson

class TeacherLessonAdapter(
    private val items: List<LessonRow>,
    private val onOpenLesson: (Lesson) -> Unit
) : RecyclerView.Adapter<TeacherLessonAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeacherLessonBinding.inflate(
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
        private val binding: ItemTeacherLessonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: LessonRow) {
            val lesson = row.lesson
            val releaseLabel = if (lesson.releasedSectionIds.isEmpty()) {
                "no section release"
            } else {
                "released to ${lesson.releasedSectionIds.joinToString(", ")}"
            }

            binding.tvUnitTitle.text = row.unitTitle
            binding.tvLessonTitle.text = lesson.title
            binding.tvLessonMeta.text =
                "${lesson.estimatedMinutes} min | ${row.activityCount} activities | $releaseLabel"
            binding.tvLessonStatus.text = lesson.status.replaceFirstChar { it.uppercase() }
            binding.btnOpenLesson.setOnClickListener { onOpenLesson(lesson) }
            binding.root.setOnClickListener { onOpenLesson(lesson) }
        }
    }

    data class LessonRow(
        val lesson: Lesson,
        val unitTitle: String,
        val activityCount: Int
    )
}
