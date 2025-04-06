package com.mithilakshar.learnsource.Activity

import PDFLoader
import android.os.Bundle
import android.util.Log

import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import com.github.barteksc.pdfviewer.PDFView
import com.mithilakshar.learnsource.Data.QuizData
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.databinding.ActivityNotesBinding
import com.mithilakshar.mithilapanchang.Dialog.Networkdialog
import com.mithilakshar.mithilapanchang.Notification.NetworkManager


class NotesActivity : AppCompatActivity() {
    lateinit var binding: ActivityNotesBinding
    private lateinit var pdfLoader: PDFLoader
    private var hasStartedNetworkTasks = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)



        setupNetworkHandling()
        val quizData = intent.getSerializableExtra("quizData") as? QuizData
        Log.d("quizData", "quizData: $quizData")

        binding.tvBookTitle.text = quizData?.data?.get("name").toString()

         var pdfView: PDFView
        pdfLoader = PDFLoader(this)
        val pdfName = quizData?.data?.get("codename").toString()
        Log.d("quizData", "codename: $quizData")
        pdfView = binding.pdfView
        pdfLoader = PDFLoader(this@NotesActivity)
        pdfLoader.loadPdf(pdfView, pdfName)


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