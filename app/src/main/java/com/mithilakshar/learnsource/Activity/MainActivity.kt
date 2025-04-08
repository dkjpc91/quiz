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
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.mithilakshar.learnsource.Adapter.SubjectAdapter
import com.mithilakshar.learnsource.Data.QuizData
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.Room.UpdatesDao
import com.mithilakshar.learnsource.Room.UpdatesDatabase
import com.mithilakshar.learnsource.Utility.GridSpacingItemDecoration
import com.mithilakshar.learnsource.Utility.UpdateChecker
import com.mithilakshar.learnsource.Utility.dbHelper

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

        setupNetworkHandling()
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

            // Make sure views are hidden initially
            binding.appbanner.visibility = View.GONE
            binding.bookContainer.visibility = View.GONE
            binding.homeviewloading.visibility = View.VISIBLE

            val onCompleteUIUpdate: () -> Unit = {
                binding.homeviewloading.visibility = View.GONE
                binding.appbanner.visibility = View.VISIBLE
                binding.bookContainer.visibility = View.VISIBLE

            }

            val loadRandomBanner: () -> Unit = {

                CoroutineScope(Dispatchers.Main).launch {


                    val dbFiles = listOf("LearnSourceMasterFile.db", "PExamsMasterFile.db")
                    val randomDbFile = dbFiles.random()
                    val tableName = randomDbFile.removeSuffix(".db")

                    val dbHelper = dbHelper(context = this@MainActivity, dbName = randomDbFile)
                    val randomRow = withContext(Dispatchers.IO) {
                        dbHelper.getRandomRowFromMasterFile(tableName)
                    }
                    delay(500) // Wait for 500ms (adjust to your needs)
                    randomRow?.let { row ->
                        (row["image"] as? String)?.let { url ->
                            Glide.with(this@MainActivity)
                                .load(url)
                                .into(binding.appbanneriv)
                        }

                        binding.bookNameText.text = row["name"] as? String ?: "Untitled Book"
                        binding.bookDescriptionText.text = row["description"] as? String ?: "No description available."
                        setupSubjectsGrid()

                        binding.bookContainer.setOnClickListener {
                            val quizData = QuizData(row)
                            Log.d("QUIZ_DATA", "randomRow: $row")
                            Log.d("QUIZ_DATA", "quizData: ${quizData.data}")
                            val intent = Intent(this@MainActivity, PreviewActivity::class.java)
                            intent.putExtra("quizData", quizData)
                            startActivity(intent)
                        }
                    }

                    // Once done, update the UI to indicate completion
                    onCompleteUIUpdate()
                }
            }


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
                        runOnUiThread {
                            loadRandomBanner() // ← Includes UI update now
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
                        runOnUiThread {
                            loadRandomBanner() // ← Includes UI update now
                        }
                    }
                )
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



    private fun setupSubjectsGrid() {
        val dbFileshome = "LearnSourceMasterFile.db"
        val dbHelperhome = dbHelper(context = this@MainActivity, dbName = dbFileshome)
        val homerows = dbHelperhome.getAllRowsFromTable("LearnSourceMasterFile").also {
            Log.d("DataLoad", "Loaded ${it.size} rows")
            it.forEachIndexed { index, row ->
                Log.d("DataLoad", "Row $index: $row")
            }
        }
        Log.d("setupSubjectsGrid", "Loaded ${homerows.size} rows from DB")

        runOnUiThread {
            val recyclerView = findViewById<RecyclerView>(R.id.homeviewsubjects)

            // Always set layout manager (remove null check)
            recyclerView.layoutManager = GridLayoutManager(this, 2).apply {
                // Optional: if you want to fix the size for better performance
                recyclerView.setHasFixedSize(true)
            }

            recyclerView.addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount = 2,
                    spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing),
                    includeEdge = true
                )
            )

            val newAdapter = SubjectAdapter(homerows) { categoryName ->
                when (categoryName) {
                    "Professional Exams" -> {
                        val intent = Intent(this, PexamActivity::class.java).apply {
                            putExtra("dbname", categoryName)
                        }
                        startActivity(intent)
                    }
                    else -> startCategoryActivity(this, categoryName)
                }
            }

            recyclerView.adapter = newAdapter
            // Notify data set changed should be called on the adapter after setting it
            newAdapter.notifyDataSetChanged()

            // Debug logging
            Log.d("RecyclerView", "Adapter item count: ${newAdapter.itemCount}")
            Log.d("RecyclerView", "RecyclerView visibility: ${recyclerView.visibility}")
            Log.d("RecyclerView", "RecyclerView measured width: ${recyclerView.measuredWidth}")
            Log.d("RecyclerView", "RecyclerView measured height: ${recyclerView.measuredHeight}")
        }
    }


}

