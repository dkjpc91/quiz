package com.mithilakshar.learnsource.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mithilakshar.learnsource.R

class SubjectAdapter(
    private var rawData: List<Map<String, Any?>>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    private val uniqueCategoryList: MutableList<String> = mutableListOf()

    init {
        updateData(rawData)
    }

    fun updateData(newData: List<Map<String, Any?>>) {
        rawData = newData
        uniqueCategoryList.clear()
        uniqueCategoryList.addAll(
            newData.mapNotNull { it["category"] as? String }
                .distinct()
                .sorted()
        )
        Log.d("SubjectAdapter", "Updated data with ${uniqueCategoryList.size} categories")
        notifyDataSetChanged()
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectIcon: ImageView = itemView.findViewById(R.id.subject_icon)
        val subjectName: TextView = itemView.findViewById(R.id.subject_name)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val category = uniqueCategoryList[adapterPosition]
                    Log.d("SubjectAdapter", "Clicked on category: $category")
                    onItemClick(category)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_grid, parent, false)
        Log.d("SubjectAdapter", "Creating new view holder")
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        if (position !in 0 until uniqueCategoryList.size) return

        val category = uniqueCategoryList[position]
        holder.subjectName.text = category
        Log.d("SubjectAdapter", "Binding category at position $position: $category")

        val iconRes = when (category.lowercase()) {
            "general knowledge" -> R.drawable.generalknowledge
            "professional exams" -> R.drawable.star
            "mathematics", "math" -> R.drawable.maths
            "computer" -> R.drawable.computer
            "chemistry" -> R.drawable.flask
            "biology" -> R.drawable.bio
            "physics" -> R.drawable.physics
            "economics" -> R.drawable.economics
            else -> R.drawable.star
        }

        holder.subjectIcon.setImageResource(iconRes)
    }

    override fun getItemCount(): Int {
        Log.d("SubjectAdapter", "Item count: ${uniqueCategoryList.size}")
        return uniqueCategoryList.size
    }
}