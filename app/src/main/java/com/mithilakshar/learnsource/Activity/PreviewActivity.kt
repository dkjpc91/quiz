package com.mithilakshar.learnsource.Activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.mithilakshar.learnsource.Data.QuizData
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.databinding.ActivityPreviewBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.mithilakshar.learnsource.Utility.PdfDbFileDownloader
import com.mithilakshar.mithilapanchang.Dialog.Networkdialog
import com.mithilakshar.mithilapanchang.Notification.NetworkManager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
class PreviewActivity : AppCompatActivity(), PdfDbFileDownloader.DownloadCallback {

    private lateinit var downloader: PdfDbFileDownloader
    private lateinit var downloadDialog: Dialog
    private var downloadedFiles = mutableListOf<File>()
    private var isDownloadComplete = false
    lateinit var binding: ActivityPreviewBinding
    private var fileCheckJob: Job? = null
    private var hasStartedNetworkTasks = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backgrounds = arrayOf(R.drawable.bg1)
        setupNetworkHandling()
        // Randomly select one of the background resources
        val randomBackground = backgrounds[Random.nextInt(backgrounds.size)]

        // Set the selected background to the root view
        binding.root.setBackgroundResource(randomBackground)

        val quizData = intent.getSerializableExtra("quizData") as? QuizData
        Log.d("quizData", "quizData: $quizData")

        binding. bookTitleTextView.text = quizData?.data?.get("name").toString()
        binding. bookdescriptionTextView.text =  quizData?.data?.get("description").toString()

        // Load the image using Glide
        Glide.with(this)
            .load(quizData?.data?.get("image").toString())
            .into(binding.bookCoverImageView)

        val Videourl = quizData?.data?.get("videourl").toString()
        val Quizurl = quizData?.data?.get("sourceurl").toString()
        val Pdfurl = quizData?.data?.get("notesurl").toString()
        val filename = quizData?.data?.get("codename").toString()

        Log.d("QuizDebug", "Video URL: $Videourl")
        Log.d("QuizDebug", "Quiz Source URL: $Quizurl")
        Log.d("QuizDebug", "PDF URL: $Pdfurl")
        Log.d("QuizDebug", "Code/File Name: $filename")


        // Video section
        if (Videourl.isNullOrBlank() || Videourl.equals("null", ignoreCase = true)) {
            Log.d("QuizDebug", "Video URL is empty or 'null'. Hiding video container.")
            binding.videoContainer.visibility = View.GONE
        } else {
            Log.d("QuizDebug", "Video URL valid: $Videourl. Showing video container.")
            binding.videoContainer.visibility = View.VISIBLE
        }

// Quiz section
        if (Quizurl.isNullOrBlank() || Quizurl.equals("null", ignoreCase = true)) {
            Log.d("QuizDebug", "Quiz URL is empty or 'null'. Hiding quiz container.")
            binding.quizContainer.visibility = View.GONE
        } else {
            Log.d("QuizDebug", "Quiz URL valid: $Quizurl. Showing quiz container.")
            binding.quizContainer.visibility = View.VISIBLE
        }

// Notes section
        if (Pdfurl.isNullOrBlank() || Pdfurl.equals("null", ignoreCase = true)) {
            Log.d("QuizDebug", "PDF URL is empty or 'null'. Hiding notes container.")
            binding.notesContainer.visibility = View.GONE
        } else {
            Log.d("QuizDebug", "PDF URL valid: $Pdfurl. Showing notes container.")
            binding.notesContainer.visibility = View.VISIBLE
        }





        downloader = PdfDbFileDownloader(this, this)

        binding.notesActionButton.setOnClickListener {
            val buttonText = binding.notesActionButton.text.toString().trim().lowercase()

            if (buttonText == "view") {
                // Do something else, like show a toast
                Toast.makeText(this, "Viewing notes - no download triggered", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, NotesActivity::class.java)
                intent.putExtra("quizData", quizData)
                this.startActivity(intent)
            } else {
                // Proceed with download
                showDownloadDialog(Pdfurl, "pdf", filename)
            }
        }



        binding.quizActionButton.setOnClickListener {
            val buttonText = binding.quizActionButton.text.toString().trim().lowercase()

            if (buttonText == "view") {
                // Do something else, like show a toast
                Toast.makeText(this, "View mode - no download needed", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, QuizActivity::class.java)
                intent.putExtra("quizData", quizData)
                this.startActivity(intent)
            } else {
                // Proceed with download
                showDownloadDialog(Quizurl, "db", filename)

            }
        }
        binding.quizDeleteIcon.setOnClickListener {
            deleteFileFromTestFolder(
                context =this@PreviewActivity,
                fileName = filename,
                extension = ".db",
                onFileDeleted = { ->
                    binding.quizDeleteIcon.visibility=View.INVISIBLE
                    Toast.makeText(this, "Deleting the file-Please wait", Toast.LENGTH_SHORT).show()
                })

        }
        binding.deleteIcon.setOnClickListener {
            deleteFileFromTestFolder(this, filename,".pdf") {
                // Callback: file deleted, update UI here
                binding.deleteIcon.visibility=View.INVISIBLE
                Toast.makeText(this, "Deleting the file- Please wait", Toast.LENGTH_SHORT).show()
            }

        }


        startFileMonitoring( quizData?.data?.get("codename").toString())

    }



    fun startFileMonitoring(codeName: String) {
        fileCheckJob?.cancel() // Cancel previous monitoring if any

        fileCheckJob = lifecycleScope.launch {
            while (isActive) {
                val results = checkCodeNameFiles(codeName)
                updateFileStatusUI(results)
                delay(5000) // Check every 5 seconds
            }
        }
    }

    fun stopFileMonitoring() {
        fileCheckJob?.cancel()
    }

    private fun updateFileStatusUI(results: Map<String, Boolean>) {
        runOnUiThread {
            results["dbExists"]?.let { exists ->
                binding.quizActionButton.text = if (exists) "View" else "Download"
                binding.quizActionButton.setTextColor(if (exists) Color.GREEN else Color.RED)
                binding.quizDeleteIcon.visibility=View.VISIBLE
            }

            results["pdfExists"]?.let { exists ->
                binding.notesActionButton.text = if (exists) "View" else "Download"
                binding.notesActionButton.setTextColor(if (exists) Color.GREEN else Color.RED)
                binding.deleteIcon.visibility=View.VISIBLE
            }
        }
    }

    private suspend fun checkCodeNameFiles(codeName: String): Map<String, Boolean> {
        return withContext(Dispatchers.IO) {
            val folderPath = getExternalFilesDir(null)?.absolutePath + File.separator + "test"
            val folder = File(folderPath).apply { if (!exists()) mkdirs() }

            val dbFile = File(folder, "$codeName.db")
            val pdfFile = File(folder, "$codeName.pdf")

            mapOf(
                "dbExists" to dbFile.exists(),
                "pdfExists" to pdfFile.exists()
            ).also {
                Log.d("FileCheck", "Checked $codeName - DB: ${it["dbExists"]}, PDF: ${it["pdfExists"]}")
            }
        }
    }



    private fun showDownloadDialog(url: String, type: String,filename:String) {
        // Initialize dialog
        downloadDialog = Dialog(this).apply {
            setContentView(R.layout.dialog_download)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setCancelable(false)

            // Setup cancel/delete button
            findViewById<Button>(R.id.cancelButton).apply {
                setOnClickListener {
                    if (isDownloadComplete) {
                        // Handle completed download case
                        deleteDownloadedFiles()
                        Toast.makeText(context, "Downloaded files deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        // Handle ongoing download case
                        downloader.cancelDownloads()
                        Toast.makeText(context, "Download cancelled", Toast.LENGTH_SHORT).show()
                    }
                    dismiss() // Close dialog in both cases
                }
            }

            // Show initial download state
            findViewById<TextView>(R.id.downloadStatusText).text = "Preparing download..."
            findViewById<LottieAnimationView>(R.id.downloadAnimation).apply {
                setAnimation(R.raw.loading)
                playAnimation()
            }

            show()
        }

        // Prepare and start download
        isDownloadComplete = false
        downloadedFiles.clear()
        downloader.downloadFilesSequentially(listOf(Triple(url, type,filename)))

        // Update UI with current file info
        downloadDialog.findViewById<TextView>(R.id.downloadStatusText).text =
            "Downloading ${type.uppercase()} file..."
    }

    private fun updateDialogProgress(fileType: String, progress: Int) {
        downloadDialog.findViewById<TextView>(R.id.downloadProgressText).text =
            "Downloading $fileType: $progress%"
    }

    private fun deleteDownloadedFiles() {
        downloadedFiles.forEach { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        downloadedFiles.clear()
    }

    // Callback implementations
    override fun onDownloadStarted(fileType: String) {
        runOnUiThread {
            downloadDialog.findViewById<TextView>(R.id.downloadStatusText).text =
                "Downloading $fileType..."
        }
    }

    override fun onProgress(fileType: String, progress: Int) {
        runOnUiThread {
            updateDialogProgress(fileType, progress)
        }
    }

    override fun onFileDownloaded(fileType: String, file: File) {
        downloadedFiles.add(file)
    }

    override fun onAllDownloadsComplete() {
        runOnUiThread {
            isDownloadComplete = true
            downloadDialog.findViewById<TextView>(R.id.downloadStatusText).text =
                "Download Complete!"
            downloadDialog.findViewById<TextView>(R.id.downloadProgressText).text =
                "${downloadedFiles.size} files downloaded"
            downloadDialog.findViewById<Button>(R.id.cancelButton).text =
                "Delete Downloads"
            downloadDialog.findViewById<LottieAnimationView>(R.id.downloadAnimation).apply {
                setAnimation(R.raw.a3)
                playAnimation()
            }

            // Auto-close after 2 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                if (downloadDialog.isShowing) {
                    downloadDialog.dismiss()
                    Toast.makeText(this@PreviewActivity,
                        "Downloads saved successfully",
                        Toast.LENGTH_SHORT).show()
                }
            }, 2000)
        }
    }

    override fun onError(fileType: String, error: String) {
        runOnUiThread {
            // Only update if dialog is still showing (wasn't cancelled)
            if (downloadDialog.isShowing) {
                downloadDialog.findViewById<TextView>(R.id.downloadStatusText).text =
                    "Download Error"
                downloadDialog.findViewById<TextView>(R.id.downloadProgressText).text =
                    error
                downloadDialog.findViewById<Button>(R.id.cancelButton).text =
                    "Close"
                downloadDialog.findViewById<LottieAnimationView>(R.id.downloadAnimation).apply {
                    setAnimation(R.raw.a1)
                    playAnimation()
                }
            }
        }
    }

    override fun onDownloadCancelled() {
        // No need for UI updates here since we're closing immediately
        // Cleanup is handled by the button click
    }



    fun deleteFileFromTestFolder(context: Context, fileName: String, extension: String, onFileDeleted: (() -> Unit)? = null) {
        val folderPath = context.getExternalFilesDir(null)?.absolutePath + File.separator + "test"
        val file = File(folderPath, "$fileName$extension")

        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Toast.makeText(context, "Deleted: $fileName$extension", Toast.LENGTH_SHORT).show()
                onFileDeleted?.invoke()
            } else {
                Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
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
                }
            }
        }
    }


}