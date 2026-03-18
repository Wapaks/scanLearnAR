package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityResultBinding
import com.example.scanlearn.models.ScannedObject
import com.example.scanlearn.models.Submission
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.*

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var storage: StorageService
    private lateinit var dbService: RealtimeDbService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        dbService = RealtimeDbService()

        val objectId = intent.getStringExtra(AppConstants.EXTRA_OBJECT_ID) ?: ""
        val objectName = intent.getStringExtra(AppConstants.EXTRA_OBJECT_NAME) ?: ""
        val score = intent.getIntExtra(AppConstants.EXTRA_SCORE, 0)
        val total = intent.getIntExtra(AppConstants.EXTRA_TOTAL, 0)
        val mode = intent.getStringExtra(AppConstants.EXTRA_MODE) ?: AppConstants.MODE_EXPLORER

        val modeColor = AppColors.getModeColor(mode)
        binding.toolbar.setBackgroundColor(modeColor)
        binding.tvObjectName.text = "Object: $objectName"
        binding.tvScore.text = "Quiz Score: $score/$total"
        binding.btnSubmit.setBackgroundColor(modeColor)

        binding.btnSubmit.setOnClickListener {
            handleSubmit(objectId, objectName, score, total, mode)
        }
    }

    private fun handleSubmit(objectId: String, objectName: String, score: Int, total: Int, mode: String) {
        val learnings = binding.etLearnings.text.toString().trim()
        if (learnings.isEmpty()) {
            Toast.makeText(this, "Please write something about what you learned", Toast.LENGTH_SHORT).show()
            return
        }

        val user = storage.getUser()
        if (user == null) {
            Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmit.isEnabled = false

        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())

        val submission = Submission(
            studentId = user.id,
            studentName = user.name,
            objectName = objectName,
            learnings = learnings,
            timestamp = timestamp,
            quizScore = score,
            totalQuestions = total,
            mode = mode
        )

        val scanned = ScannedObject(
            objectId = objectId,
            timestamp = timestamp,
            mode = mode
        )

        dbService.saveSubmission(user.id, submission) { subOk ->
            dbService.saveScannedObject(user.id, scanned) { scanOk ->
                runOnUiThread {
                    binding.btnSubmit.isEnabled = true
                    if (subOk && scanOk) {
                        Toast.makeText(this, "Submitted! Your teacher will review your reflection.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Submit failed. Check your connection and try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}