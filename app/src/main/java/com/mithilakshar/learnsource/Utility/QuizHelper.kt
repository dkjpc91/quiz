package com.mithilakshar.learnsource.Utility

import android.app.AlertDialog
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView

class QuizHelper(
    private val questionsList: List<Map<String, String>>?,
    private val context: Context,
    private val instructionsText: TextView,
    private val radioGroup: RadioGroup,
    private val radioButton1: RadioButton,
    private val radioButton2: RadioButton,
    private val radioButton3: RadioButton,
    private val radioButton4: RadioButton,
    private val submitButton: ImageButton,
    private val quizCard: CardView,
    private val scrollView: ScrollView
) {
    private var currentQuestionIndex = 0
    private var score = 0
    private var correctAnswers = 0
    private val startTime = SystemClock.elapsedRealtime()

    init {
        displayQuestion()

        // Set listener for submit button
        submitButton.setOnClickListener {
            submitAnswer()
        }
    }

    // Function to display the current question
    private fun displayQuestion() {
        if (currentQuestionIndex < questionsList!!.size) {
            val currentQuestion = questionsList?.get(currentQuestionIndex)
            val questionText = currentQuestion?.get("question") ?: ""
            val option1 = currentQuestion?.get("option1") ?: ""
            val option2 = currentQuestion?.get("option2") ?: ""
            val option3 = currentQuestion?.get("option3") ?: ""
            val option4 = currentQuestion?.get("option4") ?: ""

            // Update UI elements with the question and options
            instructionsText.text = questionText
            radioButton1.text = option1
            radioButton2.text = option2
            radioButton3.text = option3
            radioButton4.text = option4

            // Make quiz card visible
            quizCard.visibility = View.VISIBLE

            // Clear previous selections
            radioGroup.clearCheck()
        } else {
            endQuiz() // End quiz if no more questions
        }
    }

    // Function to handle answer submission
    private fun submitAnswer() {
        val selectedId = radioGroup.checkedRadioButtonId

        if (selectedId != -1) {
            // Get the selected RadioButton text
            val selectedOption = when (selectedId) {
                radioButton1.id -> radioButton1.text.toString()
                radioButton2.id -> radioButton2.text.toString()
                radioButton3.id -> radioButton3.text.toString()
                radioButton4.id -> radioButton4.text.toString()
                else -> ""
            }

            // Get correct answer for the current question
            val correctAnswer = questionsList?.get(currentQuestionIndex)?.get("correctanswer")

            // Check if the answer is correct and update score
            if (selectedOption == correctAnswer) {
                score += 1
                correctAnswers += 1
            }

            // Move to the next question
            currentQuestionIndex += 1
            displayQuestion()
        } else {
            Toast.makeText(context, "Please select an option!", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to handle quiz end
    private fun endQuiz() {
        val totalTimeTaken = (SystemClock.elapsedRealtime() - startTime) / 1000

        // Show an AlertDialog with the results
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Quiz Finished")
        builder.setMessage(
            "Score: $score/${questionsList!!.size}\n" +
                    "Correct Answers: $correctAnswers\n" +
                    "Time Taken: $totalTimeTaken seconds"
        )
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()

        // Hide quiz card after quiz ends
        quizCard.visibility = View.INVISIBLE
    }
}
