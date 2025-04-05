package com.mithilakshar.learnsource.Activity

import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.Room.UpdatesDao
import com.mithilakshar.learnsource.Room.UpdatesDatabase
import com.mithilakshar.learnsource.Utility.UpdateChecker

import com.mithilakshar.learnsource.databinding.ActivityMainBinding
import com.mithilakshar.mithilapanchang.Dialog.Networkdialog
import com.mithilakshar.mithilapanchang.Notification.NetworkManager
import com.mithilakshar.mithilapanchang.Utility.SupabaseFileDownloader
import com.mithilakshar.mithilapanchang.Utility.dbSupabaseDownloadeSequence
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var updatesDao: UpdatesDao
    private lateinit var supabaseDownloader: SupabaseFileDownloader
    private lateinit var downloadManager: dbSupabaseDownloadeSequence

    private var hasRecreated = false
    private var hasStartedNetworkTasks = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
        setupNetworkHandling()
    }

    private fun setupClickListeners() {
        val categoryButtons = mapOf(
            binding.maths to "Mathematics",
            binding.computer to "Computer Science",
            binding.chemistry to "Chemistry",
            binding.biology to "Biology",
            binding.physics to "Physics",
            binding.economics to "Economics",
            binding.gk to "General Knowledge"
        )

        categoryButtons.forEach { (button, categoryName) ->
            button.setOnClickListener {
                startCategoryActivity(this, categoryName)
            }
        }



        binding.pexams.setOnClickListener {
            val intent = Intent(this, PexamActivity::class.java).apply {
                putExtra("dbname", "Professional Exams")
            }
            startActivity(intent)
        }
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
                    performNetworkTasks()
                }
            }
        }
    }

    private fun performNetworkTasks() {
        val currentDate = LocalDate.now()
        val currentMonth = currentDate.month.name
        val currentDay = currentDate.dayOfMonth
        val currentDayName = currentDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).uppercase()

        updatesDao = UpdatesDatabase.getDatabase(applicationContext).UpdatesDao()
        supabaseDownloader = SupabaseFileDownloader(this)
        downloadManager = dbSupabaseDownloadeSequence(updatesDao, supabaseDownloader)

        val filesWithIds = listOf(
            Pair("LearnSourceMasterFile", 99),
            Pair("PExamsMasterFile", 98)
        )

        lifecycleScope.launch {
            val updateChecker = UpdateChecker(updatesDao)
            val updateStatus = updateChecker.getUpdateStatus()

            Log.d("supabase", "Update status: $updateStatus")

            withContext(Dispatchers.Main) {
                if (updateStatus != "a") {
                    Log.d("updatechecker", "Update needed: $updateStatus")

                    downloadManager.observeMultipleFileExistence(
                        filesWithIds,
                        lifecycleOwner = this@MainActivity,
                        coroutineScope = lifecycleScope,
                        homeActivity = this@MainActivity,
                        progressCallback = { progress, fileName ->
                            Log.d("Progress", "Downloading $fileName: $progress%")
                        },
                        onComplete = {
                            binding.mainview.post {
                                binding.homeviewloading.visibility = View.GONE
                                binding.appbanner.visibility = View.VISIBLE
                                binding.mainview.visibility = View.VISIBLE
                            }

                        }
                    )
                } else {
                    val missingFiles = checkFilesExistence(filesWithIds)
                    downloadManager.observeMultipleFileExistence(
                        filesWithIds = missingFiles,
                        lifecycleOwner = this@MainActivity,
                        coroutineScope = lifecycleScope,
                        homeActivity = this@MainActivity,
                        progressCallback = { progress, fileName ->
                            Log.d("FileCheck", "File: $fileName, Progress: $progress%")
                        },
                        onComplete = {
                            binding.mainview.post {
                                binding.homeviewloading.visibility = View.GONE
                                binding.appbanner.visibility = View.VISIBLE
                                binding.mainview.visibility = View.VISIBLE
                            }

                        }
                    )
                }
            }
        }
    }



    private fun startCategoryActivity(context: Context, categoryname: String) {
        val intent = Intent(context, CategoryActivity::class.java).apply {
            putExtra("categoryname", categoryname)
        }
        context.startActivity(intent)
    }

    suspend fun checkFilesExistence(filesWithIds: List<Pair<String, Int>>): List<Pair<String, Int>> {
        val missingFiles = mutableListOf<Pair<String, Int>>()
        val folderPath = getExternalFilesDir(null)?.absolutePath + File.separator + "test"
        val folder = File(folderPath).apply { if (!exists()) mkdirs() }

        coroutineScope {
            val jobs = filesWithIds.map { (fileName, fileId) ->
                launch(Dispatchers.IO) {
                    try {
                        val dbFile = File(folder, "$fileName.db")
                        if (!dbFile.exists()) {
                            synchronized(missingFiles) { missingFiles.add(fileName to fileId) }
                            Log.d("supabase", "Missing file: $fileName.db")
                        } else {
                            Log.d("supabase", "File exists: $fileName.db")
                        }
                    } catch (e: Exception) {
                        Log.e("supabase", "Error checking $fileName.db", e)
                    }
                }
            }
            jobs.joinAll()
        }

        return missingFiles
    }
}
