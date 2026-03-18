package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemStudentProgressBinding
import com.example.scanlearn.models.StudentProgress

class StudentProgressAdapter(
    private val students: List<StudentProgress>
) : RecyclerView.Adapter<StudentProgressAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemStudentProgressBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentProgressBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        with(holder.binding) {
            tvStudentName.text = student.name
            tvStudentNumber.text = student.studentNumber
            tvStudentSection.text = student.section
            tvScannedCount.text = student.scannedCount.toString()
            tvSubmissionsCount.text = student.submissionsCount.toString()
        }
    }

    override fun getItemCount() = students.size
}