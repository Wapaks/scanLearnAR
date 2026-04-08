package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.SubmissionAdapter
import com.example.scanlearn.databinding.ActivityProgressBinding
import com.example.scanlearn.models.QuizAttempt
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService

class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private lateinit var storage: StorageService
    private lateinit var dbService: RealtimeDbService
    private lateinit var authService: FirebaseAuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        dbService = RealtimeDbService()
        authService = FirebaseAuthService()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSignOut.setOnClickListener {
            authService.signOut()
            storage.clearUser()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        loadProgress()
    }

    private fun loadProgress() {
        val user = storage.getUser() ?: return

        binding.tvUserName.text = user.name
        binding.tvUserEmail.text = user.email
        binding.tvScannedCount.text = "..."
        binding.tvSubmissionsCount.text = "..."
        binding.tvAverageScore.text = "..."

        dbService.getScannedObjects(user.id) { scanned ->
            runOnUiThread {
                binding.tvScannedCount.text = scanned.size.toString()
            }
        }

        dbService.getQuizAttempts(user.id) { quizAttempts ->
            runOnUiThread {
                binding.tvAverageScore.text = buildAverageScoreLabel(quizAttempts)
            }
        }

        dbService.getSubmissions(user.id) { submissions ->
            runOnUiThread {
                binding.tvSubmissionsCount.text = submissions.size.toString()
                if (submissions.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvSubmissions.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvSubmissions.visibility = View.VISIBLE
                    binding.rvSubmissions.layoutManager = LinearLayoutManager(this)
                    binding.rvSubmissions.adapter = SubmissionAdapter(submissions)
                }
            }
        }
    }

    private fun buildAverageScoreLabel(quizAttempts: List<QuizAttempt>): String {
        if (quizAttempts.isEmpty()) return "0%"
        val totalPercent = quizAttempts.sumOf { attempt ->
            if (attempt.totalQuestions == 0) 0 else (attempt.score * 100) / attempt.totalQuestions
        }
        return "${totalPercent / quizAttempts.size}%"
    }
}
