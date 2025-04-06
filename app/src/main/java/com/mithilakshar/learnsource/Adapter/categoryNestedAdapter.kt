package com.mithilakshar.learnsource.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Add this import for image loading
import com.mithilakshar.learnsource.Activity.PreviewActivity
import com.mithilakshar.learnsource.Activity.QuizActivity
import com.mithilakshar.learnsource.Data.QuizData
import com.mithilakshar.learnsource.databinding.CategorynesteditemBinding

class categoryNestedAdapter(
    private val context: Context,
    private var categorynestedlist: List<Map<String, Any?>>
) : RecyclerView.Adapter<categoryNestedAdapter.nestedviewholder>() {

    class nestedviewholder(var binding: CategorynesteditemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun itembind(currentdata: Map<String, Any?>, context: Context) {
            // Set text from the "name" field
            currentdata["name"]?.let { name ->
                binding.titleText.text = name.toString()
            }

            // Load image from the "imageurl" field
            currentdata["image"]?.let { imageUrl ->
                Glide.with(context)
                    .load(imageUrl.toString())
                    .into(binding.imageView)
            }

            binding.root.setOnClickListener {
                val intent = Intent(context, PreviewActivity::class.java)
                val quizData = QuizData(currentdata)
                intent.putExtra("quizData", quizData)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): nestedviewholder {
        val binding = CategorynesteditemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return nestedviewholder(binding)
    }

    override fun getItemCount(): Int {
        return categorynestedlist.size
    }

    override fun onBindViewHolder(holder: nestedviewholder, position: Int) {
        val currentdata = categorynestedlist[position]
        holder.itembind(currentdata, context)
    }
}