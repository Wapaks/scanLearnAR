package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.CategoryInsightAdapter
import com.example.scanlearn.adapters.ObjectInsightAdapter
import com.example.scanlearn.databinding.ActivityTeacherAnalyticsBinding
import com.example.scanlearn.models.CategoryAnalytics
import com.example.scanlearn.models.LearningObjectAnalytics
import com.example.scanlearn.services.AiGovernanceService
import com.example.scanlearn.services.GeminiTeacherCopilotService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import kotlinx.coroutines.launch

class TeacherAnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherAnalyticsBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService
    private val aiService = GeminiTeacherCopilotService()
    private val aiGovernanceService = AiGovernanceService()
    private var latestObjectAnalytics: List<LearningObjectAnalytics> = emptyList()
    private var latestCategoryAnalytics: List<CategoryAnalytics> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()
        storageService = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnGenerateAiSummary.setOnClickListener { generateAiSummary() }
        binding.rvLowConfidence.layoutManager = LinearLayoutManager(this)
        binding.rvWeakQuiz.layoutManager = LinearLayoutManager(this)
        binding.rvCategoryConfusion.layoutManager = LinearLayoutManager(this)

        loadAnalytics()
    }

    override fun onResume() {
        super.onResume()
        loadAnalytics()
    }

    private fun loadAnalytics() {
        setLoading(true)
        dbService.getLearningObjects { objects ->
            dbService.getSubmissionsForAllStudents { submissionsMap ->
                dbService.getQuizAttemptsForAllStudents { quizAttemptsMap ->
                    dbService.getScanAttemptsForAllStudents { scanAttemptsMap ->
                        val objectAnalytics = dbService.buildLearningObjectAnalytics(
                            objects = objects,
                            submissionsMap = submissionsMap,
                            quizAttemptsMap = quizAttemptsMap,
                            scanAttemptsMap = scanAttemptsMap
                        )
                        val categoryAnalytics = dbService.buildCategoryAnalytics(
                            objects = objects,
                            quizAttemptsMap = quizAttemptsMap,
                            scanAttemptsMap = scanAttemptsMap
                        )

                        runOnUiThread {
                            setLoading(false)
                            bindAnalytics(objectAnalytics, categoryAnalytics)
                        }
                    }
                }
            }
        }
    }

    private fun bindAnalytics(
        objectAnalytics: List<LearningObjectAnalytics>,
        categoryAnalytics: List<CategoryAnalytics>
    ) {
        latestObjectAnalytics = objectAnalytics
        latestCategoryAnalytics = categoryAnalytics
        val lowConfidenceObjects = objectAnalytics
            .filter { it.lowConfidenceSelections > 0 }
            .sortedByDescending { it.lowConfidenceSelections }
            .take(5)

        val weakQuizObjects = objectAnalytics
            .filter { it.quizAttempts > 0 }
            .sortedWith(
                compareBy<LearningObjectAnalytics> { it.averageQuizScorePercent }
                    .thenByDescending { it.quizAttempts }
            )
            .take(5)

        val confusedCategories = categoryAnalytics
            .filter { it.manualCorrections > 0 || it.lowConfidenceSelections > 0 }
            .sortedWith(
                compareByDescending<CategoryAnalytics> { it.manualCorrections }
                    .thenByDescending { it.lowConfidenceSelections }
            )
            .take(5)

        binding.tvOverview.text =
            "${objectAnalytics.count()} objects - ${lowConfidenceObjects.sumOf { it.lowConfidenceSelections }} low-confidence scans - ${confusedCategories.sumOf { it.manualCorrections }} manual fixes"

        bindObjectSection(
            titleView = binding.tvLowConfidenceEmpty,
            recyclerView = binding.rvLowConfidence,
            items = lowConfidenceObjects,
            emptyText = "No low-confidence object trends yet."
        ) { item ->
            "${item.lowConfidenceSelections} low-confidence scans - ${item.manualCorrections} manual fixes"
        }

        bindObjectSection(
            titleView = binding.tvWeakQuizEmpty,
            recyclerView = binding.rvWeakQuiz,
            items = weakQuizObjects,
            emptyText = "No quiz trends yet."
        ) { item ->
            "${item.averageQuizScorePercent}% average across ${item.quizAttempts} quiz attempts"
        }

        if (confusedCategories.isEmpty()) {
            binding.tvCategoryConfusionEmpty.visibility = View.VISIBLE
            binding.rvCategoryConfusion.visibility = View.GONE
            binding.tvCategoryConfusionEmpty.text = "No manual-correction category trends yet."
        } else {
            binding.tvCategoryConfusionEmpty.visibility = View.GONE
            binding.rvCategoryConfusion.visibility = View.VISIBLE
            binding.rvCategoryConfusion.adapter = CategoryInsightAdapter(confusedCategories)
        }
    }

    private fun generateAiSummary() {
        binding.tvAiSummary.text = "Generating AI summary..."
        val currentUser = storageService.getUser()
        lifecycleScope.launch {
            try {
                val summary = aiService.summarizeAnalytics(
                    overview = binding.tvOverview.text.toString(),
                    lowConfidenceNotes = latestObjectAnalytics
                        .sortedByDescending { it.lowConfidenceSelections }
                        .take(3)
                        .map { "${it.objectName}: ${it.lowConfidenceSelections} low-confidence scans" },
                    weakQuizNotes = latestObjectAnalytics
                        .filter { it.quizAttempts > 0 }
                        .sortedBy { it.averageQuizScorePercent }
                        .take(3)
                        .map { "${it.objectName}: ${it.averageQuizScorePercent}% average" },
                    categoryNotes = latestCategoryAnalytics
                        .sortedByDescending { it.manualCorrections + it.lowConfidenceSelections }
                        .take(3)
                        .map { "${it.category}: ${it.manualCorrections} manual fixes, ${it.lowConfidenceSelections} low-confidence selections" }
                )
                aiGovernanceService.saveDraftVariant(
                    targetType = "analytics",
                    targetId = "teacher_analytics_grade3",
                    feature = "analytics_summary",
                    generatedText = summary,
                    createdBy = currentUser?.id.orEmpty(),
                    modelName = GeminiTeacherCopilotService.DEFAULT_MODEL,
                    promptVersion = GeminiTeacherCopilotService.PROMPT_VERSION
                )
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "analytics_summary",
                    targetType = "analytics",
                    targetId = "teacher_analytics_grade3",
                    modelName = GeminiTeacherCopilotService.DEFAULT_MODEL,
                    promptVersion = GeminiTeacherCopilotService.PROMPT_VERSION,
                    status = "success"
                )
                binding.tvAiSummary.text = summary
            } catch (e: Exception) {
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "analytics_summary",
                    targetType = "analytics",
                    targetId = "teacher_analytics_grade3",
                    modelName = GeminiTeacherCopilotService.DEFAULT_MODEL,
                    promptVersion = GeminiTeacherCopilotService.PROMPT_VERSION,
                    status = "failed",
                    errorMessage = e.message.orEmpty()
                )
                binding.tvAiSummary.text = "AI summary failed: ${e.message ?: "unknown error"}"
            }
        }
    }

    private fun bindObjectSection(
        titleView: android.widget.TextView,
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        items: List<LearningObjectAnalytics>,
        emptyText: String,
        metricBuilder: (LearningObjectAnalytics) -> String
    ) {
        if (items.isEmpty()) {
            titleView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            titleView.text = emptyText
        } else {
            titleView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = ObjectInsightAdapter(items, metricBuilder) { analytics ->
                val intent = Intent(this, TeacherObjectDetailActivity::class.java)
                intent.putExtra(AppConstants.EXTRA_TEACHER_OBJECT_ID, analytics.objectId)
                startActivity(intent)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentScroll.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}
