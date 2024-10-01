package com.mithilakshar.learnsource.Adapter

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mithilakshar.learnsource.databinding.CategorydetailitemBinding
import android.view.LayoutInflater
import com.mithilakshar.learnsource.Activity.QuizActivity

class categoryDetailAdapter(private val context: Context, private var categoryDetailList: List<Map<String, String>>) : RecyclerView.Adapter<categoryDetailAdapter.categoryDetailViewHolder>() {

    class categoryDetailViewHolder(var binding: CategorydetailitemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(currentData: Map<String, String>,context: Context) {
            binding.root.setOnClickListener {
                val intent= Intent(context, QuizActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): categoryDetailViewHolder {
        val binding = CategorydetailitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return categoryDetailViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoryDetailList.size
    }

    override fun onBindViewHolder(holder: categoryDetailViewHolder, position: Int) {
        val currentData = categoryDetailList[position]
        holder.bind(currentData,context)
    }
}