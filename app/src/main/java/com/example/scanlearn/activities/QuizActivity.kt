package com.example.scanlearn.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.QuizOptionAdapter
import com.example.scanlearn.databinding.ActivityQuizBinding
import com.example.scanlearn.models.QuizQuestion
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var dbService: RealtimeDbService
    private var currentQuestion = 0
    private var selectedAnswer = -1
    private val answers = mutableListOf<Int>()
    private lateinit var questions: List<QuizQuestion>
    private lateinit var objectId: String
    private lateinit var objectName: String
    private lateinit var mode: String
    private lateinit var adapter: QuizOptionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()

        objectId = intent.getStringExtra(AppConstants.EXTRA_OBJECT_ID) ?: run { finish(); return }
        mode = intent.getStringExtra(AppConstants.EXTRA_MODE) ?: AppConstants.MODE_EXPLORER

        val modeColor = AppColors.getModeColor(mode)
        binding.toolbar.setBackgroundColor(modeColor)
        binding.btnBack.setOnClickListener { finish() }
        binding.progressBar.progressTintList = ColorStateList.valueOf(modeColor)

        adapter = QuizOptionAdapter(modeColor) { index -> selectedAnswer = index }
        binding.rvOptions.layoutManager = LinearLayoutManager(this)
        binding.rvOptions.adapter = adapter
        binding.btnNext.setBackgroundColor(modeColor)

        dbService.getLearningObjects { objects ->
            val obj = objects.find { it.id == objectId } ?: run {
                runOnUiThread { finish() }
                return@getLearningObjects
            }
            objectName = obj.name
            questions = obj.quiz
            runOnUiThread {
                binding.btnNext.setOnClickListener { handleNext() }
                showQuestion()
            }
        }
    }

    private fun showQuestion() {
        val q = questions[currentQuestion]
        val total = questions.size
        binding.tvQuestionNumber.text = "Question ${currentQuestion + 1} of $total"
        binding.tvQuestion.text = q.question
        binding.progressBar.progress = ((currentQuestion + 1) * 100) / total
        binding.btnNext.text = if (currentQuestion == total - 1) "Submit Quiz" else "Next Question"
        selectedAnswer = -1
        adapter.setOptions(q.options)
    }

    private fun handleNext() {
        if (selectedAnswer == -1) {
            Toast.makeText(this, "Please choose an answer to continue", Toast.LENGTH_SHORT).show()
            return
        }
        answers.add(selectedAnswer)
        if (currentQuestion == questions.size - 1) {
            showResult()
        } else {
            currentQuestion++
            showQuestion()
        }
    }

    private fun showResult() {
        val correctCount = answers.indices.count { answers[it] == questions[it].correctAnswer }
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_OBJECT_ID, objectId)
        intent.putExtra(AppConstants.EXTRA_OBJECT_NAME, objectName)
        intent.putExtra(AppConstants.EXTRA_SCORE, correctCount)
        intent.putExtra(AppConstants.EXTRA_TOTAL, questions.size)
        intent.putExtra(AppConstants.EXTRA_MODE, mode)
        startActivity(intent)
        finish()
    }
}