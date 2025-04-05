package com.mithilakshar.learnsource.Activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.mithilakshar.learnsource.Data.QuizData
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.Utility.UrlDownloader
import com.mithilakshar.learnsource.Utility.dbHelper
import com.mithilakshar.learnsource.databinding.ActivityQuizBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var urlDownloader: UrlDownloader
    private lateinit var dbhelper: dbHelper

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val updateText: TextView = findViewById(R.id.updatetext)

        // Get quizData and extract codename
        val quizData = intent.getSerializableExtra("quizData") as? QuizData
        val codeName = quizData?.data?.get("codename")?.toString() ?: "math1"
        Log.d("codeName", "Codename: $codeName")

        // Initialize downloader
        urlDownloader = UrlDownloader(this)

        MobileAds.initialize(this) {}

        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Use the actual codename as filename
        lifecycleScope.launch {
            checkFileExistsLive(this@QuizActivity, codeName).collectLatest { exists ->
                updateText.text = if (exists) {
                    "File $codeName.db exists ✅"
                } else {
                    "File $codeName.db is missing ❌"
                }
                Log.d("supabase", "File check result for $codeName: $exists")
            }
        }

        // Sample button setup
        binding.sharequiz.setOnClickListener {
            // your logic here
        }
    }

    // Check file existence using Flow
    private fun checkFileExistsLive(context: Context, fileName: String): Flow<Boolean> = flow {
        try {
            val folderPath = context.getExternalFilesDir(null)?.absolutePath + File.separator + "test"
            val folder = File(folderPath).apply { if (!exists()) mkdirs() }
            val dbFile = File(folder, "$fileName.db")
            val exists = dbFile.exists()
            emit(exists)
        } catch (e: Exception) {
            Log.e("supabase", "Error checking $fileName.db", e)
            emit(false)
        }
    }.flowOn(Dispatchers.IO)
}
