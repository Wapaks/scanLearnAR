package com.example.scanlearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemFactBinding

class FactsAdapter(private val facts: List<String>) :
    RecyclerView.Adapter<FactsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvFact.text = facts[position]
    }

    override fun getItemCount() = facts.size
}
