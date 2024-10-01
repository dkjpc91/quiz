package com.mithilakshar.learnsource.Activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mithilakshar.learnsource.Adapter.categoryAdapter
import com.mithilakshar.learnsource.Adapter.categoryDetailAdapter
import com.mithilakshar.learnsource.Data.categorynestedlistdataclass
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.Utility.dbHelper
import com.mithilakshar.learnsource.databinding.ActivityCategoryDetailBinding
import java.io.Serializable

class CategoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryDetailBinding
    private lateinit var categorydetailadapter: categoryDetailAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityCategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


         val category = intent.getSerializableExtra("nestedCategoryList") as? categorynestedlistdataclass
        Log.d("nestedCategoryList", "table name $category")

        val nestedCategoryList = category?.categories
        categorydetailadapter= nestedCategoryList?.let { categoryDetailAdapter(this, it )}!!
        recyclerView=binding.categorydetailrecycler

        recyclerView.adapter=categorydetailadapter

    }


}