package com.mithilakshar.learnsource.Activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mithilakshar.learnsource.Data.QuizData
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.Utility.QuizHelper
import com.mithilakshar.learnsource.Utility.UrlDownloader
import com.mithilakshar.learnsource.Utility.dbHelper
import com.mithilakshar.learnsource.databinding.ActivityQuizBinding
import java.io.File

class QuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizBinding
    private lateinit var urlDownloader: UrlDownloader
    private lateinit var dbhelper: dbHelper


    private var fileExists=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val instructionsText: TextView = findViewById(R.id.instructionsText)
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        val radioButton1: RadioButton = findViewById(R.id.radioButton1)
        val radioButton2: RadioButton = findViewById(R.id.radioButton2)
        val radioButton3: RadioButton = findViewById(R.id.radioButton3)
        val radioButton4: RadioButton = findViewById(R.id.radioButton4)
        val submitButton: ImageButton = findViewById(R.id.submitButton)
        val quizCard: CardView = findViewById(R.id.quizcard)
        val scrollView: ScrollView = findViewById(R.id.scrollView)

        val quizData = intent.getSerializableExtra("quizData") as? QuizData
        urlDownloader=UrlDownloader(this@QuizActivity)

        Log.d("quizData", " : quizData $quizData")
        val filename= quizData?.data?.get("filename")
        val url= quizData?.data?.get("dburl")
        Log.d("quizData", " : filename : url  $filename, $url")
        fileExists = doesFileExist(this, "$filename.db")
        Log.d("quizData", " : fileExists  $fileExists")
        if (fileExists) {

            binding.updatetext.visibility=View.GONE
            binding.redownloadquiz.visibility=View.VISIBLE
            binding.deletequiz.visibility=View.VISIBLE
            binding.downloadquiz.visibility=View.GONE
            binding.playquiz.visibility=View.VISIBLE
            dbhelper=dbHelper(this@QuizActivity, "$filename.db")
            val subjectlist= filename?.let { dbhelper.getAllRows(it) }
            Log.d("quizData", " :$subjectlist")
            val quizHelper = QuizHelper(
                subjectlist,
                this,
                instructionsText,
                radioGroup,
                radioButton1,
                radioButton2,
                radioButton3,
                radioButton4,
                submitButton,
                quizCard,
                scrollView
            )

        } else {
            binding.updatetext.text="Download quiz"
            binding.redownloadquiz.visibility=View.GONE
            binding.deletequiz.visibility=View.GONE
        }

        binding.downloadquiz.setOnClickListener {
            binding.updatetext.text ="Downloading"
            binding.downloadquiz.visibility=View.GONE
            urlDownloader.downloadFile(
                url =url.toString() ,
                filename = "$filename.db",
                progressCallback = { progress, totalBytes ->
                    // Update UI with download progress
                    runOnUiThread {
                        binding.updatetext.text="Downloading progress- $progress %"
                    }

                },
                completionCallback = { success, file ->
                    if (success) {
                        runOnUiThread {
                            binding.updatetext.visibility = View.GONE
                            binding.redownloadquiz.visibility = View.VISIBLE
                            binding.deletequiz.visibility = View.VISIBLE
                            binding.downloadquiz.visibility = View.GONE
                            binding.playquiz.visibility = View.VISIBLE
                            dbhelper=dbHelper(this@QuizActivity, "$filename.db")
                            val subjectlist= filename?.let { dbhelper.getAllRows(it) }
                            Log.d("quizData", " :$subjectlist")
                            val quizHelper = QuizHelper(
                                subjectlist,
                                this,
                                instructionsText,
                                radioGroup,
                                radioButton1,
                                radioButton2,
                                radioButton3,
                                radioButton4,
                                submitButton,
                                quizCard,
                                scrollView
                            )
                        }

                    } else {
                        Log.d("quizData", " : quizData download failed")
                    }
                }
            )

        }
        binding.redownloadquiz.setOnClickListener {
            binding.updatetext.text ="Downloading"
                urlDownloader.redownloadFile(
                url = url.toString(),
                filename = "$filename.db",
                progressCallback = { progress, totalBytes ->
                    runOnUiThread {
                        binding.updatetext.text = "Downloading progress- $progress %"
                    }
                },
                completionCallback = { success, file ->
                    if (success) {
                        runOnUiThread {
                            binding.updatetext.visibility = View.GONE
                            binding.redownloadquiz.visibility = View.VISIBLE
                            binding.deletequiz.visibility = View.VISIBLE
                            binding.downloadquiz.visibility = View.GONE
                            binding.playquiz.visibility = View.VISIBLE
                            dbhelper=dbHelper(this@QuizActivity, "$filename.db")
                            val subjectlist= filename?.let { dbhelper.getAllRows(it) }
                            Log.d("quizData", " :$subjectlist")
                            val quizHelper = QuizHelper(
                                subjectlist,
                                this,
                                instructionsText,
                                radioGroup,
                                radioButton1,
                                radioButton2,
                                radioButton3,
                                radioButton4,
                                submitButton,
                                quizCard,
                                scrollView
                            )
                        }
                    } else {
                        Log.d("quizData", " : quizData download failed")
                    }
                }
            )
        }
        binding.deletequiz.setOnClickListener {
            binding.updatetext.text ="Download quiz"
            urlDownloader.deleteFile("$filename.db"){ success, message ->

                fileExists = doesFileExist(this, "$filename.db")
                if (fileExists) {
                    runOnUiThread {
                        binding.updatetext.visibility = View.GONE
                        binding.redownloadquiz.visibility = View.VISIBLE
                        binding.deletequiz.visibility = View.VISIBLE
                        binding.downloadquiz.visibility = View.GONE
                        binding.playquiz.visibility = View.VISIBLE
                    }

                } else {
                    runOnUiThread {
                        binding.updatetext.text = "Download quiz"
                        binding.redownloadquiz.visibility = View.GONE
                        binding.deletequiz.visibility = View.GONE
                    }
                }
            }

        }

        binding.sharequiz.setOnClickListener {


        }



    }


    fun doesFileExist(context: Context, filename: String): Boolean {
        // Define the download directory
        val downloadDirectory = File(context.getExternalFilesDir(null), "test")

        // Check if the directory exists
        if (!downloadDirectory.exists()) {
            return false
        }

        // Create the file object for the specified filename
        val fileToCheck = File(downloadDirectory, filename)

        // Return whether the file exists
        return fileToCheck.exists()
    }
}