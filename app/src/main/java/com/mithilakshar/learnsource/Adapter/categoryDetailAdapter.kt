package com.mithilakshar.learnsource.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mithilakshar.learnsource.databinding.CategorydetailitemBinding
import android.view.LayoutInflater
import com.mithilakshar.learnsource.Activity.QuizActivity
import com.bumptech.glide.Glide // Add this for image loading
import com.mithilakshar.learnsource.Activity.PreviewActivity
import com.mithilakshar.learnsource.Data.QuizData

class categoryDetailAdapter(
    private val context: Context,
    private var categoryDetailList: List<Map<String, String>>
) : RecyclerView.Adapter<categoryDetailAdapter.categoryDetailViewHolder>() {

    class categoryDetailViewHolder(var binding: CategorydetailitemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(currentData: Map<String, String>, context: Context) {
            // Set title from "name" column
            binding.mBookTitle.text = currentData["name"]

            // Set description from "description" column
            binding.mdescription.text = currentData["description"]

            // Load image from "image" column using Glide
            currentData["image"]?.let { imageUrl ->
                Glide.with(context)
                    .load(imageUrl)
                    .into(binding.mBookImage)
            }

            binding.root.setOnClickListener {
                val intent = Intent(context, PreviewActivity::class.java)
                val quizData = QuizData(currentData)
                intent.putExtra("quizData", quizData)
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
        holder.bind(currentData, context)
    }
}