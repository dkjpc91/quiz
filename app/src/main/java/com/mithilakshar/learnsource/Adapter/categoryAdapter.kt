package com.mithilakshar.learnsource.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mithilakshar.learnsource.Activity.CategoryDetailActivity
import com.mithilakshar.learnsource.Data.categorynestedlistdataclass
import com.mithilakshar.learnsource.databinding.CategoryitemBinding

class categoryAdapter(
    private val context: Context,
    private val subcategoryList: List<Map<String, Any?>>
) : RecyclerView.Adapter<categoryAdapter.categoryViewHolder>() {

    private val uniquesubCategoryList = getUniqueCategoryList(subcategoryList)

    inner class categoryViewHolder(val binding: CategoryitemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            categoryMap: Map<String, Any>,
            fullList: List<Map<String, Any?>>
        ) {
            // Safely get and display the subcategory name
            val subcategoryName = categoryMap["subcategory"]?.toString()?.trim() ?: "Unknown Category"
            binding.categoryTitle.text = subcategoryName.replaceFirstChar {
                if (it.isLowerCase()) it.uppercaseChar() else it
            }

            // Filter items for this specific subcategory
            val nestedList = fullList.filter {
                it["subcategory"]?.toString()?.trim() == subcategoryName
            }

            // Setup nested recycler view
            val nestedAdapter = categoryNestedAdapter(context, nestedList)
            binding.nestedcategoryrecycler.adapter = nestedAdapter

            // Handle See All click
            binding.seeAllBtn.setOnClickListener {
                val intent = Intent(context, CategoryDetailActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    // Convert data if needed
                    val categoryData = categorynestedlistdataclass(
                        nestedList.map { it.mapValues { entry -> entry.value?.toString() ?: "" } }
                    )
                    putExtra("nestedCategoryList", categoryData)
                    putExtra("categoryTitle", subcategoryName) // Pass the title too if needed
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): categoryViewHolder {
        val binding = CategoryitemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return categoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: categoryViewHolder, position: Int) {
        val categoryMap = uniquesubCategoryList[position]
        holder.bind(categoryMap, subcategoryList)
    }

    override fun getItemCount(): Int = uniquesubCategoryList.size

    private fun getUniqueCategoryList(list: List<Map<String, Any?>>): List<Map<String, Any>> {
        return list
            .mapNotNull { item ->
                item["subcategory"]?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    item as? Map<String, Any>
                }
            }
            .distinctBy { it["subcategory"]?.toString()?.trim()?.lowercase() }
    }
}