package com.mithilakshar.learnsource.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mithilakshar.learnsource.Activity.CategoryDetailActivity
import com.mithilakshar.learnsource.Activity.QuizActivity
import com.mithilakshar.learnsource.Data.QuizData
import com.mithilakshar.learnsource.Data.categorynestedlistdataclass
import com.mithilakshar.learnsource.databinding.CategorynesteditemBinding


class categoryNestedAdapter(private val context: Context, private var  categorynestedlist: List<Map<String, String>>) :  RecyclerView.Adapter<categoryNestedAdapter.nestedviewholder>(){


    class nestedviewholder(var binding: CategorynesteditemBinding):RecyclerView.ViewHolder(binding.root){
        fun itembind(currentdata: Map<String, String>, context: Context) {

            binding.root.setOnClickListener {
                val intent = Intent(context, QuizActivity::class.java)
                val quizData = QuizData(currentdata)
                intent.putExtra("quizData", quizData)
                context.startActivity(intent)
            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): nestedviewholder {
        var binding= CategorynesteditemBinding.inflate( LayoutInflater.from(parent.context), parent, false)
        return nestedviewholder(binding)
    }

    override fun getItemCount(): Int {
        return categorynestedlist.size
    }

    override fun onBindViewHolder(holder: nestedviewholder, position: Int) {
        val currentdata=categorynestedlist.get(position)
        return holder.itembind(currentdata,context)
    }
}