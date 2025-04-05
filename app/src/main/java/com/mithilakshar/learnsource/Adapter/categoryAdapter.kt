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

    class categoryViewHolder(val binding: CategoryitemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            categoryMap: Map<String, Any>,
            fullList: List<Map<String, Any?>>,
            context: Context
        ) {
            val subcategoryName = categoryMap["subcategory"]?.toString() ?: ""
            binding.categoryTitle.text = subcategoryName.replaceFirstChar { it.uppercaseChar() }

            // Use only items matching this category (no conversion)
            val nestedList = fullList.filter { it["subcategory"]?.toString() == subcategoryName }

            // Pass as-is to nested adapter (assuming it accepts Map<String, Any>)
            val nestedAdapter = categoryNestedAdapter(context, nestedList)
            binding.nestedcategoryrecycler.adapter = nestedAdapter

            // Handle See All click
            binding.seeAllBtn.setOnClickListener {
                val intent = Intent(context, CategoryDetailActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // Still requires conversion if categorynestedlistdataclass expects Map<String, String>
                val categoryData = categorynestedlistdataclass(
                    nestedList.map { it.mapValues { entry -> entry.value.toString() } }
                )
                intent.putExtra("nestedCategoryList", categoryData)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): categoryViewHolder {
        val binding = CategoryitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return categoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: categoryViewHolder, position: Int) {
        val categoryMap = uniquesubCategoryList[position]
        holder.bind(categoryMap, subcategoryList, context)
    }

    override fun getItemCount(): Int = uniquesubCategoryList.size

    private fun getUniqueCategoryList(list: List<Map<String, Any?>>): List<Map<String, Any>> {
        val seenCategories = mutableSetOf<String>()
        val uniqueList = mutableListOf<Map<String, Any>>()

        for (item in list) {
            val category = item["subcategory"]?.toString()
            if (category != null && seenCategories.add(category)) {
                uniqueList.add(item as Map<String, Any>)
            }
        }
        return uniqueList
    }
}
