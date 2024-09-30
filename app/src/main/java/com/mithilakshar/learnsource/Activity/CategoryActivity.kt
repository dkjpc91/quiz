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
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.Utility.dbHelper
import com.mithilakshar.learnsource.databinding.ActivityCategoryBinding

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryadapter: categoryAdapter
    private lateinit var dbhelper: dbHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val dbName = intent.getStringExtra("dbname")
        Log.d("dbName", "db name $dbName.db")
        Log.d("dbName", "table name $dbName")
        dbhelper=dbHelper(this@CategoryActivity, "$dbName.db")

        val subjectlist= dbhelper.getAllRows("maths")

        Log.d("dbName", "table name $subjectlist")
       categoryadapter= subjectlist?.let { categoryAdapter(this, it,dbhelper,dbName) }!!
        recyclerView=binding.categoryrecycler
       recyclerView.layoutManager=LinearLayoutManager(this)
       recyclerView.adapter=categoryadapter
    }
}