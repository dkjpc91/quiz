package com.mithilakshar.learnsource.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mithilakshar.learnsource.Utility.dbHelper
import com.mithilakshar.learnsource.databinding.CategoryitemBinding

class categoryAdapter(
    private val context: Context,
    private val categorylist: List<Map<String, String>>,
    private var dbHelper: dbHelper,
    private val     dbName: String?
) :  RecyclerView.Adapter<categoryAdapter.categoryViewHolder>(){

    val uniqueCategoryList = getUniqueCategoryList(categorylist)

    class categoryViewHolder(val binding: CategoryitemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Map<String, String>,context: Context,categorylist:List<Map<String, String>>) {
            binding.categoryTitle.text = category["category"]
            val categoryvalue= category["category"]
            val nestedCategoryList = categoryvalue?.let { filterByCategory(categorylist, it) }
            val nestedAdapter = nestedCategoryList?.let { categoryNestedAdapter(context, it) }
             binding.nestedcategoryrecycler .adapter = nestedAdapter

        }

        fun filterByCategory(categoryList: List<Map<String, String>>, categoryValue: String): List<Map<String, String>> {
            return categoryList.filter { it["category"] == categoryValue }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): categoryViewHolder {
        var binding= CategoryitemBinding.inflate( LayoutInflater.from(parent.context), parent, false)
        return categoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return uniqueCategoryList.size
    }

    override fun onBindViewHolder(holder: categoryViewHolder, position: Int) {
        val currentdata=uniqueCategoryList.get(position)
        return holder.bind(currentdata, context,categorylist)
    }





    fun getUniqueCategoryList(categoryList: List<Map<String, String>>): List<Map<String, String>> {
        val uniqueCategories = mutableSetOf<String>()
        val uniqueList = mutableListOf<Map<String, String>>()

        for (item in categoryList) {
            val category = item["category"]
            // Check if the category is unique
            if (category != null && uniqueCategories.add(category)) {
                uniqueList.add(item) // Add the whole map for the first occurrence of the category
            }
        }
        return uniqueList
    }


}

