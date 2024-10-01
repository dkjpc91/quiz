package com.mithilakshar.learnsource.Adapter

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mithilakshar.learnsource.databinding.CategorydetailitemBinding
import android.view.LayoutInflater
import com.mithilakshar.learnsource.Activity.CategoryActivity
import com.mithilakshar.learnsource.Activity.CategoryDetailActivity
import com.mithilakshar.learnsource.Activity.QuizActivity
import com.mithilakshar.learnsource.Data.categorynestedlistdataclass
import com.mithilakshar.learnsource.databinding.PexamitemBinding

class pexamAdapter(private val context: Context, private var pexamList: List<Map<String, String>>) : RecyclerView.Adapter<pexamAdapter.pexamViewHolder>() {

    val uniqueCategoryList = getUniquesubjectList(pexamList)

    class pexamViewHolder(var binding: PexamitemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(currentData: Map<String, String>,context: Context,pexamList: List<Map<String, String>>) {

            binding.pexamtext.text=currentData["subject"]


            binding.root.setOnClickListener {
                val intent= Intent(context, CategoryActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("dbname", "pexams")
                context.startActivity(intent)
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): pexamViewHolder {
        val binding = PexamitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return pexamViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return uniqueCategoryList.size
    }

    override fun onBindViewHolder(holder: pexamViewHolder, position: Int) {
        val currentData = uniqueCategoryList[position]
        holder.bind(currentData,context,pexamList)
    }

    fun getUniquesubjectList(categoryList: List<Map<String, String>>): List<Map<String, String>> {
        val uniqueCategories = mutableSetOf<String>()
        val uniqueList = mutableListOf<Map<String, String>>()

        for (item in categoryList) {
            val subject = item["subject"]
            // Check if the category is unique
            if (subject != null && uniqueCategories.add(subject)) {
                uniqueList.add(item) // Add the whole map for the first occurrence of the category
            }
        }
        return uniqueList
    }

}