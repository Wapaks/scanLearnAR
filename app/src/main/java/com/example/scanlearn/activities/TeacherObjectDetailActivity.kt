package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityTeacherObjectDetailBinding
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.LearningObjectAnalytics
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TeacherObjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherObjectDetailBinding
    private lateinit var dbService: RealtimeDbService

    private var objectId: String = ""
    private var currentObject: LearningObject? = null
    private var currentAnalytics: LearningObjectAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherObjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()
        objectId = intent.getStringExtra(AppConstants.EXTRA_TEACHER_OBJECT_ID) ?: ""

        binding.btnBack.setOnClickListener { finish() }
        binding.btnEditObject.setOnClickListener { openEdit() }
        binding.btnToggleStatus.setOnClickListener { toggleStatus() }

        loadObjectDetail()
    }

    override fun onResume() {
        super.onResume()
        if (objectId.isNotBlank()) {
            loadObjectDetail()
        }
    }

    private fun loadObjectDetail() {
        if (objectId.isBlank()) {
            finish()
            return
        }

        setLoading(true)
        dbService.getLearningObject(objectId) { learningObject ->
            if (learningObject == null) {
                runOnUiThread {
                    setLoading(false)
                    Toast.makeText(this, "Object not found.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@getLearningObject
            }

            currentObject = learningObject

            dbService.getSubmissionsForAllStudents { submissionsMap ->
                dbService.getQuizAttemptsForAllStudents { quizAttemptsMap ->
                    dbService.getScanAttemptsForAllStudents { scanAttemptsMap ->
                        currentAnalytics = dbService.buildLearningObjectAnalytics(
                            objects = listOf(learningObject),
                            submissionsMap = submissionsMap,
                            quizAttemptsMap = quizAttemptsMap,
                            scanAttemptsMap = scanAttemptsMap
                        ).firstOrNull()

                        runOnUiThread {
                            setLoading(false)
                            bindObject()
                        }
                    }
                }
            }
        }
    }

    private fun bindObject() {
        val objectItem = currentObject ?: return
        val analytics = currentAnalytics ?: LearningObjectAnalytics(
            objectId = objectItem.id,
            objectName = objectItem.name,
            category = objectItem.category,
            status = objectItem.status.ifBlank { "published" }
        )

        val status = objectItem.status.ifBlank { "published" }

        binding.tvObjectName.text = objectItem.name
        binding.tvCategory.text = objectItem.category.replaceFirstChar { it.uppercase() }
        binding.tvStatus.text = status.replaceFirstChar { it.uppercase() }
        binding.tvDescription.text = objectItem.description
        binding.tvFactsCount.text = "${objectItem.facts.size} fact(s)"
        binding.tvQuizCount.text = "${objectItem.quiz.size} quiz question(s)"
        binding.tvScansValue.text = analytics.totalScanSelections.toString()
        binding.tvLowConfidenceValue.text = analytics.lowConfidenceSelections.toString()
        binding.tvManualValue.text = analytics.manualCorrections.toString()
        binding.tvQuizAvgValue.text = "${analytics.averageQuizScorePercent}%"
        binding.tvLearnersValue.text = analytics.recentLearners.toString()
        binding.tvQuizAttemptsValue.text = analytics.quizAttempts.toString()

        binding.tvLowConfidenceHint.text = if (analytics.lowConfidenceSelections == 0) {
            "Students are identifying this object clearly so far."
        } else {
            "${analytics.lowConfidenceSelections} low-confidence selections suggest this object may need better aliases or clearer sample images."
        }

        binding.tvWeakQuizHint.text = if (analytics.quizAttempts == 0) {
            "No quiz attempts yet for this object."
        } else if (analytics.averageQuizScorePercent < 60) {
            "Quiz performance is weak. Consider simplifying the lesson or revising the quiz."
        } else {
            "Quiz performance looks healthy for this object."
        }

        binding.btnToggleStatus.text = if (status.equals("archived", ignoreCase = true)) {
            "Publish Object"
        } else {
            "Archive Object"
        }
    }

    private fun openEdit() {
        val objectItem = currentObject ?: return
        val intent = Intent(this, AddObjectActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_TEACHER_OBJECT_ID, objectItem.id)
        startActivity(intent)
    }

    private fun toggleStatus() {
        val objectItem = currentObject ?: return
        val nextStatus = if (objectItem.status.equals("archived", ignoreCase = true)) {
            "published"
        } else {
            "archived"
        }

        binding.progressBar.visibility = View.VISIBLE
        dbService.updateLearningObjectStatus(
            objectId = objectItem.id,
            status = nextStatus,
            updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        ) { success ->
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                if (success) {
                    currentObject = objectItem.copy(status = nextStatus)
                    bindObject()
                    Toast.makeText(this, "Object marked as ${nextStatus}.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Could not update object status.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}
