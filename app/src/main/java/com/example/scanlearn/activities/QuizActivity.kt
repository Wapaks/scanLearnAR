package com.example.scanlearn.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.QuizOptionAdapter
import com.example.scanlearn.databinding.ActivityQuizBinding
import com.example.scanlearn.models.QuizAttempt
import com.example.scanlearn.models.QuizQuestion
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService
    private var currentQuestion = 0
    private var selectedAnswer = -1
    private val answers = mutableListOf<Int>()
    private lateinit var questions: List<QuizQuestion>
    private lateinit var objectId: String
    private lateinit var objectName: String
    private lateinit var mode: String
    private var missionId: String = ""
    private lateinit var adapter: QuizOptionAdapter
    private var scanAttemptId: String = ""
    private var scanConfidence: Float = 0f
    private var manualCorrection: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()
        storageService = StorageService(this)

        objectId = intent.getStringExtra(AppConstants.EXTRA_OBJECT_ID) ?: run { finish(); return }
        mode = intent.getStringExtra(AppConstants.EXTRA_MODE) ?: AppConstants.MODE_EXPLORER
        missionId = intent.getStringExtra(AppConstants.EXTRA_MISSION_ID) ?: ""
        scanAttemptId = intent.getStringExtra(AppConstants.EXTRA_SCAN_ATTEMPT_ID) ?: ""
        scanConfidence = intent.getFloatExtra(AppConstants.EXTRA_SCAN_CONFIDENCE, 0f)
        manualCorrection = intent.getBooleanExtra(AppConstants.EXTRA_SCAN_MANUAL_CORRECTION, false)

        val modeColor = AppColors.getModeColor(mode)
        binding.toolbar.setBackgroundColor(modeColor)
        binding.btnBack.setOnClickListener { finish() }
        binding.progressBar.progressTintList = ColorStateList.valueOf(modeColor)

        adapter = QuizOptionAdapter(modeColor) { index -> selectedAnswer = index }
        binding.rvOptions.layoutManager = LinearLayoutManager(this)
        binding.rvOptions.adapter = adapter
        binding.btnNext.setBackgroundColor(modeColor)

        dbService.getPublishedLearningObjects { objects ->
            val obj = objects.find { it.id == objectId } ?: run {
                runOnUiThread { finish() }
                return@getPublishedLearningObjects
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
        val user = storageService.getUser()
        val correctCount = answers.indices.count { answers[it] == questions[it].correctAnswer }
        if (user == null) {
            Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        val quizAttempt = QuizAttempt(
            studentId = user.id,
            objectId = objectId,
            objectName = objectName,
            mode = mode,
            score = correctCount,
            totalQuestions = questions.size,
            answers = answers.toList(),
            scanAttemptId = scanAttemptId,
            completedAt = timestamp
        )

        dbService.saveQuizAttempt(user.id, quizAttempt) { quizAttemptId ->
            runOnUiThread {
                if (quizAttemptId == null) {
                    Toast.makeText(this, "Could not save quiz progress. Please try again.", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra(AppConstants.EXTRA_OBJECT_ID, objectId)
                intent.putExtra(AppConstants.EXTRA_OBJECT_NAME, objectName)
                intent.putExtra(AppConstants.EXTRA_SCORE, correctCount)
                intent.putExtra(AppConstants.EXTRA_TOTAL, questions.size)
                intent.putExtra(AppConstants.EXTRA_MODE, mode)
                intent.putExtra(AppConstants.EXTRA_MISSION_ID, missionId)
                intent.putExtra(AppConstants.EXTRA_SCAN_ATTEMPT_ID, scanAttemptId)
                intent.putExtra(AppConstants.EXTRA_SCAN_CONFIDENCE, scanConfidence)
                intent.putExtra(AppConstants.EXTRA_SCAN_MANUAL_CORRECTION, manualCorrection)
                intent.putExtra(AppConstants.EXTRA_QUIZ_ATTEMPT_ID, quizAttemptId)
                startActivity(intent)
                finish()
            }
        }
    }
}
