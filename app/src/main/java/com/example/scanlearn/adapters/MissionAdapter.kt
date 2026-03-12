package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemMissionBinding
import com.example.scanlearn.models.Mission

class MissionAdapter(
    private val missions: List<Mission>,
    private val onStart: (Mission) -> Unit
) : RecyclerView.Adapter<MissionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMissionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mission = missions[position]
        holder.binding.tvTitle.text = mission.title
        holder.binding.tvDescription.text = mission.description
        holder.binding.tvObjects.text = "Objects to find: ${mission.objectsToFind.joinToString(", ")}"

        if (mission.completed) {
            holder.binding.tvCompleted.visibility = View.VISIBLE
            holder.binding.root.alpha = 0.7f
        } else {
            holder.binding.tvCompleted.visibility = View.GONE
            holder.binding.root.alpha = 1.0f
        }

        holder.binding.root.setOnClickListener { onStart(mission) }
    }

    override fun getItemCount() = missions.size
}
