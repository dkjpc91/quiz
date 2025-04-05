package com.mithilakshar.learnsource.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mithilakshar.learnsource.Activity.CategoryDetailActivity
import com.mithilakshar.learnsource.Data.categorynestedlistdataclass
import com.mithilakshar.learnsource.databinding.CategoryitemBinding

class PExamsAdapter(
    private val context: Context,
    private val fullDataList: List<Map<String, Any?>>
) : RecyclerView.Adapter<PExamsAdapter.ExamsViewHolder>() {

    private val uniqueCategories = getUniqueCategories(fullDataList)

    inner class ExamsViewHolder(val binding: CategoryitemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String) {
            val filteredList = fullDataList.filter {
                it["category"]?.toString().equals(category, ignoreCase = true)
            }

            binding.categoryTitle.text = category.replaceFirstChar { it.uppercaseChar() }

            val nestedAdapter = categoryNestedAdapter(context, filteredList)
            binding.nestedcategoryrecycler.adapter = nestedAdapter

            binding.seeAllBtn.setOnClickListener {
                val intent = Intent(context, CategoryDetailActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                val categoryData = categorynestedlistdataclass(
                    filteredList.map { it.mapValues { entry -> entry.value.toString() } }
                )
                intent.putExtra("nestedCategoryList", categoryData)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamsViewHolder {
        val binding = CategoryitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExamsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExamsViewHolder, position: Int) {
        holder.bind(uniqueCategories[position])
    }

    override fun getItemCount(): Int = uniqueCategories.size

    private fun getUniqueCategories(dataList: List<Map<String, Any?>>): List<String> {
        val seen = mutableSetOf<String>()
        val unique = mutableListOf<String>()

        for (item in dataList) {
            val category = item["category"]?.toString()
            if (!category.isNullOrEmpty() && seen.add(category)) {
                unique.add(category)
            }
        }

        return unique
    }
}
