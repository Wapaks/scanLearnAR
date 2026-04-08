package com.example.scanlearn.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.LowConfidenceScanAdapter
import com.example.scanlearn.adapters.QuizAttemptAdapter
import com.example.scanlearn.adapters.SubmissionAdapter
import com.example.scanlearn.adapters.WeakTopicAdapter
import com.example.scanlearn.databinding.ActivityStudentDetailBinding
import com.example.scanlearn.models.LowConfidenceScanInsight
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.QuizAttempt
import com.example.scanlearn.models.ScanAttempt
import com.example.scanlearn.models.StudentProgress
import com.example.scanlearn.models.Submission
import com.example.scanlearn.models.WeakTopicInsight
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDetailBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService
    private lateinit var student: StudentProgress
    private var lowConfidenceItems: List<LowConfidenceScanInsight> = emptyList()
    private var weakCategoryItems: List<WeakTopicInsight> = emptyList()
    private var weakObjectItems: List<WeakTopicInsight> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()
        storageService = StorageService(this)
        student = intent.getSerializableExtra(AppConstants.EXTRA_STUDENT_PROGRESS) as? StudentProgress
            ?: run {
                finish()
                return
            }

        binding.btnBack.setOnClickListener { finish() }
        bindHeader()
        bindActions()
        loadDetails()
    }

    private fun bindHeader() {
        binding.tvStudentName.text = student.name
        binding.tvStudentMeta.text = "${student.section} | ${student.studentNumber}"
        binding.tvAverageScore.text = "${student.averageScorePercent}%"
        binding.tvManualCorrections.text = student.manualCorrectionsCount.toString()
        binding.tvLowConfidence.text = student.lowConfidenceCount.toString()
    }

    private fun loadDetails() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.contentGroup.visibility = View.GONE

        dbService.getLearningObjects { objects ->
            val objectMap = objects.associateBy { it.id }
            dbService.getSubmissions(student.userId) { submissions ->
                dbService.getQuizAttempts(student.userId) { quizAttempts ->
                    dbService.getScanAttempts(student.userId) { scanAttempts ->
                        runOnUiThread {
                            binding.loadingIndicator.visibility = View.GONE
                            binding.contentGroup.visibility = View.VISIBLE
                            bindRecentLearned(submissions)
                            bindQuizHistory(quizAttempts)
                            bindScanInsights(scanAttempts, objectMap)
                            bindWeakTopics(quizAttempts, objectMap)
                        }
                    }
                }
            }
        }
    }

    private fun bindActions() {
        binding.btnAssignFollowUpMission.setOnClickListener {
            if (weakObjectItems.isEmpty() && weakCategoryItems.isEmpty()) {
                Toast.makeText(this, "Not enough quiz data yet to prepare a follow-up mission.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            assignFollowUpMission()
        }

        binding.btnReviewLowConfidence.setOnClickListener {
            if (lowConfidenceItems.isEmpty()) {
                Toast.makeText(this, "No low-confidence scans to review for this student.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.lowConfidenceSection.visibility = View.VISIBLE
            binding.lowConfidenceSection.post {
                binding.lowConfidenceSection.requestFocus()
            }
        }
    }

    private fun bindRecentLearned(submissions: List<Submission>) {
        val recent = submissions.take(5)
        if (recent.isEmpty()) {
            binding.tvRecentEmpty.visibility = View.VISIBLE
            binding.rvRecentLearned.visibility = View.GONE
        } else {
            binding.tvRecentEmpty.visibility = View.GONE
            binding.rvRecentLearned.visibility = View.VISIBLE
            binding.rvRecentLearned.layoutManager = LinearLayoutManager(this)
            binding.rvRecentLearned.adapter = SubmissionAdapter(recent)
        }
    }

    private fun bindQuizHistory(quizAttempts: List<QuizAttempt>) {
        val recent = quizAttempts.take(5)
        if (recent.isEmpty()) {
            binding.tvQuizEmpty.visibility = View.VISIBLE
            binding.rvQuizHistory.visibility = View.GONE
        } else {
            binding.tvQuizEmpty.visibility = View.GONE
            binding.rvQuizHistory.visibility = View.VISIBLE
            binding.rvQuizHistory.layoutManager = LinearLayoutManager(this)
            binding.rvQuizHistory.adapter = QuizAttemptAdapter(recent)
        }
    }

    private fun bindScanInsights(
        scanAttempts: List<ScanAttempt>,
        objectMap: Map<String, LearningObject>
    ) {
        val total = scanAttempts.size
        val manual = scanAttempts.count { it.manualCorrection }
        val low = scanAttempts.count { it.confidence in 0.0001f..0.54f }
        val manualTrend = if (total == 0) 0 else (manual * 100) / total
        val lowTrend = if (total == 0) 0 else (low * 100) / total

        binding.tvScanSummary.text = if (total == 0) {
            "No confirmed scan attempts yet."
        } else {
            "$manualTrend% manual corrections, $lowTrend% low-confidence scans across $total attempts."
        }

        lowConfidenceItems = scanAttempts
            .filter { it.confidence in 0.0001f..0.54f }
            .take(5)
            .map { attempt ->
                val obj = objectMap[attempt.selectedObjectId]
                LowConfidenceScanInsight(
                    objectName = obj?.name ?: attempt.selectedObjectId.ifBlank { "Unknown Object" },
                    category = obj?.category ?: attempt.categoryContext,
                    confidencePercent = (attempt.confidence * 100).toInt(),
                    manualCorrection = attempt.manualCorrection,
                    createdAt = attempt.createdAt
                )
            }

        if (lowConfidenceItems.isEmpty()) {
            binding.tvLowConfidenceEmpty.visibility = View.VISIBLE
            binding.rvLowConfidenceScans.visibility = View.GONE
        } else {
            binding.tvLowConfidenceEmpty.visibility = View.GONE
            binding.rvLowConfidenceScans.visibility = View.VISIBLE
            binding.rvLowConfidenceScans.layoutManager = LinearLayoutManager(this)
            binding.rvLowConfidenceScans.adapter = LowConfidenceScanAdapter(lowConfidenceItems)
        }
    }

    private fun bindWeakTopics(
        quizAttempts: List<QuizAttempt>,
        objectMap: Map<String, LearningObject>
    ) {
        weakObjectItems = quizAttempts
            .groupBy { it.objectId.ifBlank { it.objectName.ifBlank { "unknown" } } }
            .map { (objectId, attempts) ->
                val objectName = attempts.firstOrNull()?.objectName?.ifBlank { "Unknown Object" } ?: "Unknown Object"
                val average = attempts.map {
                    if (it.totalQuestions == 0) 0 else (it.score * 100) / it.totalQuestions
                }.average().toInt()
                WeakTopicInsight(
                    itemId = objectId,
                    objectName = objectName,
                    averageScorePercent = average,
                    attemptsCount = attempts.size
                )
            }
            .sortedWith(compareBy<WeakTopicInsight> { it.averageScorePercent }.thenByDescending { it.attemptsCount })
            .take(5)

        if (weakObjectItems.isEmpty()) {
            binding.tvWeakTopicsEmpty.visibility = View.VISIBLE
            binding.rvWeakTopics.visibility = View.GONE
        } else {
            binding.tvWeakTopicsEmpty.visibility = View.GONE
            binding.rvWeakTopics.visibility = View.VISIBLE
            binding.rvWeakTopics.layoutManager = LinearLayoutManager(this)
            binding.rvWeakTopics.adapter = WeakTopicAdapter(weakObjectItems)
        }

        weakCategoryItems = quizAttempts
            .groupBy { attempt ->
                objectMap[attempt.objectId]?.category?.replaceFirstChar { it.uppercase() } ?: "Unknown"
            }
            .map { (categoryName, attempts) ->
                val average = attempts.map {
                    if (it.totalQuestions == 0) 0 else (it.score * 100) / it.totalQuestions
                }.average().toInt()
                WeakTopicInsight(
                    itemId = categoryName.lowercase(),
                    objectName = categoryName,
                    averageScorePercent = average,
                    attemptsCount = attempts.size
                )
            }
            .sortedWith(compareBy<WeakTopicInsight> { it.averageScorePercent }.thenByDescending { it.attemptsCount })
            .take(5)

        if (weakCategoryItems.isEmpty()) {
            binding.tvWeakCategoriesEmpty.visibility = View.VISIBLE
            binding.rvWeakCategories.visibility = View.GONE
        } else {
            binding.tvWeakCategoriesEmpty.visibility = View.GONE
            binding.rvWeakCategories.visibility = View.VISIBLE
            binding.rvWeakCategories.layoutManager = LinearLayoutManager(this)
            binding.rvWeakCategories.adapter = WeakTopicAdapter(weakCategoryItems)
        }
    }

    private fun showFollowUpMissionDraft() {
        val weakCategory = weakCategoryItems.firstOrNull()
        val weakObjects = weakObjectItems.take(3)
        val categoryLabel = weakCategory?.objectName ?: "Mixed Review"
        val objectsLabel = if (weakObjects.isEmpty()) {
            "Focus on the student's recent low-scoring quiz objects."
        } else {
            "Recommended objects: " + weakObjects.joinToString(", ") { it.objectName }
        }

        binding.cardFollowUpMission.visibility = View.VISIBLE
        binding.tvFollowUpTitle.text = "Follow-up Mission: $categoryLabel Reinforcement"
        binding.tvFollowUpReason.text = if (weakCategory != null) {
            "This student's weakest category is $categoryLabel with an average score of ${weakCategory.averageScorePercent}%."
        } else {
            "This student needs more guided review based on recent quiz performance."
        }
        binding.tvFollowUpObjects.text = objectsLabel
    }

    private fun assignFollowUpMission() {
        val weakCategory = weakCategoryItems.firstOrNull()
        val weakObjects = weakObjectItems.take(3)
        val categoryLabel = weakCategory?.objectName ?: "Mixed Review"
        val teacher = storageService.getUser()
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        val missionId = "mission_" + student.userId + "_" + System.currentTimeMillis()
        val objectIds = weakObjects.mapNotNull { it.itemId.takeIf { value -> value.isNotBlank() && value != "unknown" } }

        val mission = Mission(
            id = missionId,
            title = "${student.name} - $categoryLabel Reinforcement",
            description = "Follow-up mission created for ${student.name} to review low-scoring topics in $categoryLabel.",
            objectsToFind = objectIds.ifEmpty { listOf("review_required") },
            sectionIds = listOf(student.section),
            category = categoryLabel.lowercase(),
            active = true,
            createdBy = teacher?.id ?: "",
            createdAt = timestamp,
            recommendedForStudentId = student.userId
        )

        binding.btnAssignFollowUpMission.isEnabled = false
        dbService.saveMission(mission) { success ->
            runOnUiThread {
                binding.btnAssignFollowUpMission.isEnabled = true
                if (success) {
                    showFollowUpMissionDraft()
                    Toast.makeText(
                        this,
                        "Follow-up mission assigned to ${student.section} section.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Could not save the follow-up mission. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
