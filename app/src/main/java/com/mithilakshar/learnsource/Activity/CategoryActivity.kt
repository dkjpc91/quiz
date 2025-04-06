package com.mithilakshar.learnsource.Activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
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
import com.mithilakshar.mithilapanchang.Dialog.Networkdialog
import com.mithilakshar.mithilapanchang.Notification.NetworkManager

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryadapter: categoryAdapter
    private lateinit var dbhelper: dbHelper
    private var hasStartedNetworkTasks = false

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

        setupNetworkHandling()
        val categoryname = intent.getStringExtra("categoryname")
        Log.d("dbName", "db name $categoryname.db")
        Log.d("dbName", "table name $categoryname")
        val dbName = "LearnSourceMasterFile.db"
        dbhelper = dbHelper(this, dbName)

        val groupedData = categoryname?.let { dbhelper.getRowsByCategoryname(it) }
        Log.d("DBHelperDebug", "Grouped Data for '': $groupedData")
        groupedData?.forEachIndexed { index, row ->
            Log.d("DBHelperDebug", "Row $index: $row")
        }

       categoryadapter = groupedData?.let { categoryAdapter(this, it) }!!
      recyclerView=binding.categoryrecycler
       recyclerView.layoutManager=LinearLayoutManager(this)
       recyclerView.adapter=categoryadapter

        displayGroupedData(this, groupedData, binding.alertTextView,recyclerView)
    }


    private fun setupNetworkHandling() {
        val networkDialog = Networkdialog(this)
        val networkManager = NetworkManager(this)

        networkManager.observe(this) { isConnected ->
            if (!isConnected) {
                if (!networkDialog.isShowing) networkDialog.show()
            } else {
                if (networkDialog.isShowing) networkDialog.dismiss()

                if (!hasStartedNetworkTasks) {
                    hasStartedNetworkTasks = true
                }
            }
        }
    }

    fun displayGroupedData(
        context: Context,
        groupedData: List<Map<String, Any?>>?,
        textView: TextView,
        recyclerView: RecyclerView
    ) {
        if (groupedData.isNullOrEmpty()) {
            // Show alert-style message in a "card"
            textView.apply {
                text = """
                ⚠️ Update will be available soon.
            """.trimIndent()

                textSize = 16f
                setPadding(32, 48, 32, 48)
                setTextColor(context.getColor(android.R.color.black))
                setBackgroundResource(R.drawable.bg1) // Use a drawable for rounded card effect
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.star, 0, 0, 0) // optional left icon
                compoundDrawablePadding = 16
            }

            recyclerView.visibility=View.GONE
        } else {
            // Handle/display data
            textView.text = "✅ Data successfully loaded."
            textView.setBackgroundResource(0) // Remove card if needed
            textView.visibility=View.GONE
            recyclerView.visibility=View.VISIBLE
        }
    }

}