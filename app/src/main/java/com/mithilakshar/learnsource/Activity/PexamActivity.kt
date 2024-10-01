package com.mithilakshar.learnsource.Activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mithilakshar.learnsource.Adapter.categoryDetailAdapter
import com.mithilakshar.learnsource.Adapter.pexamAdapter
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.Utility.dbHelper
import com.mithilakshar.learnsource.databinding.ActivityPexamBinding

class PexamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPexamBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbhelper: dbHelper
    private lateinit var pexamAdapter: pexamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityPexamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dbName = intent.getStringExtra("dbname")
        dbhelper=dbHelper(this@PexamActivity, "$dbName.db")
        Log.d("dbname", "File: $dbName,")
        Log.d("dbname", "File:  $dbName.db")
        val subjectlist= dbName?.let { dbhelper.getAllRows(it) }
        Log.d("dbname", "File:  $subjectlist")

        pexamAdapter= subjectlist?.let { pexamAdapter(this, it )}!!
        recyclerView=binding.pexamrecycler
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter=pexamAdapter

    }
}