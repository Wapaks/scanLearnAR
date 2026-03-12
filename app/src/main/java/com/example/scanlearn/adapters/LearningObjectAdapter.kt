package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemLearningObjectBinding
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.utils.AppColors

class LearningObjectAdapter(
    private val objects: List<LearningObject>,
    private val mode: String,
    private val onClick: (LearningObject) -> Unit
) : RecyclerView.Adapter<LearningObjectAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLearningObjectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLearningObjectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obj = objects[position]
        val modeColor = AppColors.getModeColor(mode)
        holder.binding.tvName.text = obj.name
        holder.binding.tvCategory.text = obj.category
        holder.binding.viewAccent.setBackgroundColor(modeColor)
        holder.binding.root.setOnClickListener { onClick(obj) }
    }

    override fun getItemCount() = objects.size
}
