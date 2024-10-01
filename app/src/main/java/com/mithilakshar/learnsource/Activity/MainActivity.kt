package com.mithilakshar.learnsource.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.Room.UpdatesDao
import com.mithilakshar.learnsource.Room.UpdatesDatabase
import com.mithilakshar.learnsource.Utility.FirebaseFileDownloader
import com.mithilakshar.learnsource.Utility.UpdateChecker
import com.mithilakshar.learnsource.Utility.dbDownloadersequence
import com.mithilakshar.learnsource.databinding.ActivityMainBinding
import com.mithilakshar.learnsource.databinding.ActivitySplashBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var fileDownloader: FirebaseFileDownloader
    private lateinit var dbDownloadersequence: dbDownloadersequence

    private lateinit var updatesDao: UpdatesDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
         binding.maths.setOnClickListener {
             startCategoryActivity(this,"maths" )
         }
        binding.computer.setOnClickListener {
            startCategoryActivity(this,"computer" )
        }
        binding.chemistry.setOnClickListener {
            startCategoryActivity(this,"chemistry" )
        }
        binding.biology.setOnClickListener {
            startCategoryActivity(this,"biology" )
        }
        binding.physics.setOnClickListener {
            startCategoryActivity(this,"physics" )
        }
        binding.economics.setOnClickListener {
            startCategoryActivity(this,"economics" )
        }
        binding.gk.setOnClickListener {
            startCategoryActivity(this,"gk" )
        }

        binding.pexams.setOnClickListener {
            val intent = Intent(this, PexamActivity::class.java)
            intent.putExtra("dbname", "pexams")
            startActivity(intent)
        }



        fileDownloader = FirebaseFileDownloader(this)
        updatesDao = UpdatesDatabase.getDatabase(applicationContext).UpdatesDao()
        dbDownloadersequence = dbDownloadersequence(updatesDao, fileDownloader)

        val filesWithIds = listOf(
            Pair("maths", 1),
            Pair("pexams", 2),
        )

        lifecycleScope.launch {
            val updateChecker = UpdateChecker(updatesDao)
            val isUpdateNeeded = updateChecker.getUpdateStatus()
            if (isUpdateNeeded!="a") {

                Log.d("updatechecker", " :  needed $isUpdateNeeded")

                dbDownloadersequence.observeMultipleFileExistence(
                    filesWithIds,
                    this@MainActivity,
                    lifecycleScope,
                    homeActivity = this@MainActivity, // Your activity
                    progressCallback = { progress, filePair  ->

                        Log.d("updatechecker", " :  individual file downloading $isUpdateNeeded")

                        Log.d("Progress", "File: $filePair, Progress: $progress%")


                    },{
                        Log.d("updatechecker", " :  all file downloaded $isUpdateNeeded")
                        Log.d("updatechecker", " :  needed $isUpdateNeeded")


                    }
                )


            } else {

                Log.d("updatechecker", " : not needed $isUpdateNeeded")
                Log.d("updatechecker", " :  no update required $isUpdateNeeded")
            }
        }


    }





    fun startCategoryActivity(context: Context, dbName: String) {
        val intent = Intent(context, CategoryActivity::class.java)
        intent.putExtra("dbname", dbName)
        context.startActivity(intent)
    }
}