package com.example.scanlearn.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanlearn.databinding.ItemQuizOptionBinding

class QuizOptionAdapter(
    private val modeColor: Int,
    private val onSelect: (Int) -> Unit
) : RecyclerView.Adapter<QuizOptionAdapter.ViewHolder>() {

    private var options: List<String> = emptyList()
    private var selectedIndex = -1

    inner class ViewHolder(val binding: ItemQuizOptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setOptions(newOptions: List<String>) {
        options = newOptions
        selectedIndex = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuizOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.binding.tvOption.text = option

        val isSelected = selectedIndex == position

        if (isSelected) {
            holder.binding.root.setStrokeColor(ColorStateList.valueOf(modeColor))  // FIXED
            holder.binding.root.strokeWidth = 6
            holder.binding.root.setCardBackgroundColor(
                Color.argb(30, Color.red(modeColor), Color.green(modeColor), Color.blue(modeColor))
            )
            holder.binding.radioIndicator.backgroundTintList = ColorStateList.valueOf(modeColor)
        } else {
            holder.binding.root.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E0E0E0")))  // FIXED
            holder.binding.root.strokeWidth = 2
            holder.binding.root.setCardBackgroundColor(Color.WHITE)
            holder.binding.radioIndicator.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
        }

        // FIXED: use getAdapterPosition() instead of capturing position directly
        holder.binding.root.setOnClickListener {
            val adapterPosition = holder.getAdapterPosition()
            if (adapterPosition == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            val prev = selectedIndex
            selectedIndex = adapterPosition
            onSelect(adapterPosition)
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(adapterPosition)
        }
    }

    override fun getItemCount() = options.size
}