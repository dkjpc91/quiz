package com.mithilakshar.learnsource.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.mithilakshar.learnsource.Data.QuizData
import com.mithilakshar.learnsource.R
import com.mithilakshar.learnsource.Utility.*
import com.mithilakshar.learnsource.databinding.ActivityQuizBinding
import com.mithilakshar.mithilapanchang.Dialog.Networkdialog
import com.mithilakshar.mithilapanchang.Notification.NetworkManager

class QuizActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "QuizActivity"
    }

    private lateinit var binding: ActivityQuizBinding
    private lateinit var dbhelper: dbHelper
    private lateinit var quizManager: QuizManager
    private var hasStartedNetworkTasks = false


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

        setupAds()
        setupAnimation()
        setupNetworkHandling()
        val quizData = intent.getSerializableExtra("quizData") as? QuizData
        val codeName = quizData?.data?.get("codename")?.toString() ?: "math1"
        val dbName = "$codeName.db"
        dbhelper = dbHelper(this, dbName)

        val questions: List<Map<String, Any?>> = dbhelper.quizdbdata(codeName)
        Log.d(TAG, "Loaded ${questions.size} questions")

        // Show quiz UI and hide animation
        binding.quizplaceholder.visibility = View.VISIBLE
        binding.lottieView.visibility = View.GONE

        binding.sharequiz.setOnClickListener {
            val randomValue = (1..100).random()
            Log.d("ShareDebug", "Random Value: $randomValue")

            if (randomValue <= 50) {
                ViewShareUtil.shareViewAsImageDirectly(binding.quizcard, this)
            } else {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Check out this app")
                    putExtra(Intent.EXTRA_TEXT, "Download our app: https://play.google.com/store/apps/details?id=com.mithilakshar.learnsource")
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }





        }

        quizManager = QuizManager(
            context = this,
            questions = questions,
            questionTextView = binding.instructionsText,
            radioGroup = binding.radioGroup,
            optionA = binding.radioButton1,
            optionB = binding.radioButton2,
            optionC = binding.radioButton3,
            optionD = binding.radioButton4,
            submitButton = binding.submitButton,
            scoreTextView = binding.scoreText,
            timerTextView = binding.timerText,
            restartButton = binding.retakequiz,
            quizplaceholder = binding.quizplaceholder,
            lottieView = binding.lottieView,

        )

        quizManager.startQuiz()
    }

    private fun setupAds() {
        Log.d(TAG, "Initializing ads")
        MobileAds.initialize(this)
        binding.adView.loadAd(AdRequest.Builder().build())
    }

    private fun setupAnimation() {
        Log.d(TAG, "Playing Lottie animation")


        binding.lottieView.playAnimation()
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()

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
