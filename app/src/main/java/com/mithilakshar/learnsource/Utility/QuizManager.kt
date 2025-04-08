package com.mithilakshar.learnsource.Utility

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import com.mithilakshar.learnsource.R

class QuizManager(
    private val context: Context,
    private val questions: List<Map<String, Any?>>,
    private val questionTextView: TextView,
    private val radioGroup: RadioGroup,
    private val optionA: RadioButton,
    private val optionB: RadioButton,
    private val optionC: RadioButton,
    private val optionD: RadioButton,
    private val submitButton: ImageView,
    private val scoreTextView: TextView,
    private val timerTextView: TextView,
    private val restartButton: LinearLayout,
    private val quizplaceholder: LinearLayout,
    private val  lottieView: LottieAnimationView
) {

    private var currentQuestionIndex = 0
    private var score = 0
    private var elapsedTime = 0 // in seconds
    private val maxTime = 3600 // 1 hour

    private val userAnswers = mutableListOf<String>()
    private val correctAnswers = mutableListOf<String>()
    private val explanations = mutableListOf<String>()

    private val handler = Handler()
    private lateinit var timerRunnable: Runnable

    fun startQuiz() {
        score = 0
        currentQuestionIndex = 0
        elapsedTime = 0
        userAnswers.clear()
        correctAnswers.clear()
        explanations.clear()

        startTimer()
        loadQuestion()

        submitButton.setOnClickListener { handleSubmit() }
        restartButton.setOnClickListener { startQuiz()
            score=0
            scoreTextView.text = "Score: $score"
        }
    }

    private fun loadQuestion() {
        if (currentQuestionIndex >= questions.size) {
            endQuiz()
            return
        }

        val question = questions[currentQuestionIndex]
        questionTextView.text = "Q${currentQuestionIndex + 1}. ${question["Question"]}"

        optionA.text = "A. ${question["OptionA"]}"
        optionB.text = "B. ${question["OptionB"]}"
        optionC.text = "C. ${question["OptionC"]}"
        optionD.text = "D. ${question["OptionD"]}"

        radioGroup.clearCheck()
    }

    private fun handleSubmit() {
        val selectedOptionId = radioGroup.checkedRadioButtonId
        if (selectedOptionId == -1) {
            Toast.makeText(context, "Please select an option", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedOption = when (selectedOptionId) {
            optionA.id -> "A"
            optionB.id -> "B"
            optionC.id -> "C"
            optionD.id -> "D"
            else -> ""
        }

        val question = questions[currentQuestionIndex]
        val correctAnswer = question["Answer"].toString()
        val explanation = question["Explanation"].toString()

        userAnswers.add(selectedOption)
        correctAnswers.add(correctAnswer)
        explanations.add(explanation)

        if (selectedOption == correctAnswer) {
            score++
            scoreTextView.text = "Score: $score"
        }

        currentQuestionIndex++
        loadQuestion()
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                val minutes = elapsedTime / 60
                val seconds = elapsedTime % 60
                timerTextView.text = String.format("Time: %02d:%02d", minutes, seconds)

                if (elapsedTime >= maxTime) {
                    Toast.makeText(context, "⏰ Time's up! Submitting quiz automatically.", Toast.LENGTH_LONG).show()
                    endQuiz()
                    return
                }

                elapsedTime++
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable)
    }

    private fun endQuiz() {
        handler.removeCallbacks(timerRunnable)
        showSummaryDialog()
    }

    private fun showSummaryDialog() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_quiz_summary, null)
        val summaryContainer = dialogView.findViewById<LinearLayout>(R.id.summaryContainer)

        // 🎯 Final Score Header
        val scoreHeader = TextView(context).apply {
            text = "🎯 Final Score: $score / ${questions.size}"
            textSize = 20f
            setTextColor(context.getColor(android.R.color.holo_green_dark))
            setPadding(16, 16, 16, 32)
        }
        summaryContainer.addView(scoreHeader)

        // 📋 Question-wise Feedback
        for (i in questions.indices) {
            val question = questions[i]["Question"].toString()
            val userAns = userAnswers.getOrNull(i) ?: "-"
            val correctAns = correctAnswers.getOrNull(i) ?: "-"
            val explanation = explanations.getOrNull(i) ?: "-"

            val block = TextView(context).apply {
                text = """
                    Q${i + 1}. $question
                    
                    ❓ Your Answer: $userAns
                    ✅ Correct Answer: $correctAns
                    ℹ️ Explanation: $explanation
                """.trimIndent()
                textSize = 16f
                setPadding(16, 16, 16, 24)
            }

            summaryContainer.addView(block)
        }

        builder.setView(dialogView)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            quizplaceholder.visibility = View.GONE
            lottieView.visibility = View.VISIBLE
        }
        builder.setCancelable(false)
        builder.show()
    }

    fun pauseTimer() {
        handler.removeCallbacks(timerRunnable)
    }

    fun resumeTimer() {
        handler.post(timerRunnable)
    }
}
