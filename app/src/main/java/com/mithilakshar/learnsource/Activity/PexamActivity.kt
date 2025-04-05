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
import com.mithilakshar.learnsource.Adapter.PExamsAdapter
import com.mithilakshar.learnsource.Adapter.categoryDetailAdapter
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.Utility.dbHelper
import com.mithilakshar.learnsource.databinding.ActivityPexamBinding

class PexamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPexamBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbhelper: dbHelper

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
        val dbName = "PExamsMasterFile.db"
        val categoryname = intent.getStringExtra("categoryname")
        dbhelper=dbHelper(this@PexamActivity, dbName)

        val groupedData = dbhelper.getAllRowsFromMasterFile()
        val adapter = groupedData?.let { PExamsAdapter(this, it) }

        Log.d("DBHelperDebug", "Grouped Data for '': $groupedData")
        groupedData?.forEachIndexed { index, row ->
            Log.d("DBHelperDebug", "Row $index: $row")
        }
        Log.d("dbname", "File: $dbName,")
        Log.d("dbname", "File:  $dbName.db")



        recyclerView=binding.pexamrecycler
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        recyclerView.adapter=adapter

    }
}