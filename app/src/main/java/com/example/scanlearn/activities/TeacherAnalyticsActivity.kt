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
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.MissionAnalytics
import com.example.scanlearn.models.StudentLessonProgress
import com.example.scanlearn.models.StudentMissionProgress
import com.example.scanlearn.models.User
import com.example.scanlearn.services.AiGovernanceService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.services.TeacherCopilotDefaults
import com.example.scanlearn.services.TeacherCopilotService
import com.example.scanlearn.services.TeacherCopilotServiceFactory
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.SchoolStructure
import kotlinx.coroutines.launch

class TeacherAnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherAnalyticsBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService
    private val aiService: TeacherCopilotService = TeacherCopilotServiceFactory.create()
    private val aiGovernanceService = AiGovernanceService()
    private var latestObjectAnalytics: List<LearningObjectAnalytics> = emptyList()
    private var latestCategoryAnalytics: List<CategoryAnalytics> = emptyList()
    private var latestMissionAnalytics: List<MissionAnalytics> = emptyList()
    private var latestMissions: List<Mission> = emptyList()
    private var latestMissionProgressMaps: Map<String, Map<String, StudentMissionProgress>> = emptyMap()
    private var remediationMissionId: String = ""
    private var remediationLessonId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()
        storageService = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnGenerateAiSummary.setOnClickListener { generateAiSummary() }
        binding.btnOpenRemediationLesson.setOnClickListener { openRemediationLesson() }
        binding.btnOpenRemediationMission.setOnClickListener { openRemediationMission() }
        binding.btnOpenRemediationSection.setOnClickListener {
            startActivity(Intent(this, TeacherStudentsActivity::class.java))
        }
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
        val teacherGrade = currentTeacherGrade()
        dbService.getAllStudents { students ->
            val gradeStudents = students.filter { it.gradeLevel.equals(teacherGrade, ignoreCase = true) }
            val studentIds = gradeStudents.map { it.id }.toSet()
            dbService.getLearningObjects { objects ->
                dbService.getSubmissionsForAllStudents { submissionsMap ->
                    dbService.getQuizAttemptsForAllStudents { quizAttemptsMap ->
                        dbService.getScanAttemptsForAllStudents { scanAttemptsMap ->
                            dbService.getAllStudentLessonProgressMaps { lessonProgressMaps ->
                                dbService.getAllMissions { missions ->
                                    dbService.getAllStudentMissionProgressMaps { missionProgressMaps ->
                                        val filteredSubmissions = submissionsMap.filterKeys { it in studentIds }
                                        val filteredQuizAttempts = quizAttemptsMap.filterKeys { it in studentIds }
                                        val filteredScanAttempts = scanAttemptsMap.filterKeys { it in studentIds }
                                        val filteredLessonProgress = lessonProgressMaps.filterKeys { it in studentIds }
                                        val filteredMissions = missions.filter {
                                            it.gradeLevel.equals(teacherGrade, ignoreCase = true)
                                        }
                                        val filteredMissionProgress = missionProgressMaps.filterKeys { it in studentIds }
                                        val objectAnalytics = dbService.buildLearningObjectAnalytics(
                                            objects = objects,
                                            submissionsMap = filteredSubmissions,
                                            quizAttemptsMap = filteredQuizAttempts,
                                            scanAttemptsMap = filteredScanAttempts
                                        )
                                        val categoryAnalytics = dbService.buildCategoryAnalytics(
                                            objects = objects,
                                            quizAttemptsMap = filteredQuizAttempts,
                                            scanAttemptsMap = filteredScanAttempts
                                        )
                                        val missionAnalytics = dbService.buildMissionAnalytics(
                                            filteredMissions,
                                            filteredMissionProgress
                                        )

                                        runOnUiThread {
                                            setLoading(false)
                                            bindAnalytics(
                                                teacherGrade = teacherGrade,
                                                gradeStudents = gradeStudents,
                                                lessonProgressMaps = filteredLessonProgress,
                                                objectAnalytics = objectAnalytics,
                                                categoryAnalytics = categoryAnalytics,
                                                missions = filteredMissions,
                                                missionAnalytics = missionAnalytics,
                                                missionProgressMaps = filteredMissionProgress
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindAnalytics(
        teacherGrade: String,
        gradeStudents: List<User>,
        lessonProgressMaps: Map<String, Map<String, StudentLessonProgress>>,
        objectAnalytics: List<LearningObjectAnalytics>,
        categoryAnalytics: List<CategoryAnalytics>,
        missions: List<Mission>,
        missionAnalytics: List<MissionAnalytics>,
        missionProgressMaps: Map<String, Map<String, StudentMissionProgress>>
    ) {
        latestObjectAnalytics = objectAnalytics
        latestCategoryAnalytics = categoryAnalytics
        latestMissions = missions
        latestMissionAnalytics = missionAnalytics
        latestMissionProgressMaps = missionProgressMaps
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

        val completedLessons = lessonProgressMaps.values.sumOf { progressMap ->
            progressMap.values.count { it.status.equals("completed", ignoreCase = true) }
        }
        val completedMissionRuns = missionProgressMaps.values.sumOf { progressMap ->
            progressMap.values.count { it.completed }
        }
        val activeMissionRuns = missionProgressMaps.values.sumOf { progressMap ->
            progressMap.values.count { !it.completed }
        }
        binding.tvOverview.text =
            "$teacherGrade - ${gradeStudents.size} learners - $completedLessons lessons done - $completedMissionRuns tasks done - $activeMissionRuns task runs active"
        bindMissionOutcomes(missions, missionAnalytics, missionProgressMaps)

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

    private fun bindMissionOutcomes(
        missions: List<Mission>,
        missionAnalytics: List<MissionAnalytics>,
        missionProgressMaps: Map<String, Map<String, StudentMissionProgress>>
    ) {
        val quarterLinked = missions.count { it.quarterId.isNotBlank() }
        val allMissionRuns = missionProgressMaps.values.flatMap { it.values }
        val completedMissionRuns = allMissionRuns.count { it.completed }
        val activeMissionRuns = allMissionRuns.count { !it.completed }
        val topStalled = missionAnalytics.filter { it.stuckLearners > 0 }.take(3)
        val topRemediationMission = missionAnalytics
            .filter { it.stuckLearners > 0 }
            .maxByOrNull { it.stuckLearners }

        binding.tvMissionOutcomeSummary.text =
            "$quarterLinked curriculum-linked missions are active. Learners have completed $completedMissionRuns mission runs, with $activeMissionRuns still in progress."
        binding.tvMissionOutcomeTop.text = if (topStalled.isEmpty()) {
            "No stalled mission patterns yet. Current mission flow looks healthy."
        } else {
            topStalled.joinToString("\n") { analytics ->
                "${analytics.title}: ${analytics.completedLearners}/${analytics.assignedLearners} completed, ${analytics.stuckLearners} learner(s) stalled."
            }
        }
        binding.tvRemediationSummary.text = if (topRemediationMission == null) {
            remediationMissionId = ""
            remediationLessonId = ""
            "No remediation alert yet. Keep watching lesson-linked missions as more students complete them."
        } else {
            val relatedMission = missions.firstOrNull { it.id == topRemediationMission.missionId }
            remediationMissionId = relatedMission?.id.orEmpty()
            remediationLessonId = relatedMission?.lessonIds?.firstOrNull().orEmpty()
            val linkedLesson = remediationLessonId.replace("_", " ").replaceFirstChar { it.uppercase() }
                .ifBlank { "the linked lesson" }
            "Remediation priority: revisit $linkedLesson for ${topRemediationMission.stuckLearners} learner(s), then assign a follow-up mission or guided retry."
        }
        binding.btnOpenRemediationLesson.isEnabled = remediationLessonId.isNotBlank()
        binding.btnOpenRemediationMission.isEnabled = remediationMissionId.isNotBlank()
    }

    private fun openRemediationLesson() {
        if (remediationLessonId.isBlank()) return
        startActivity(Intent(this, LessonStudioActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_LESSON_ID, remediationLessonId)
        })
    }

    private fun openRemediationMission() {
        if (remediationMissionId.isBlank()) return
        startActivity(Intent(this, AddEditMissionActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_MISSION_ID, remediationMissionId)
        })
    }

    private fun generateAiSummary() {
        binding.tvAiSummary.text = "Generating AI summary..."
        val currentUser = storageService.getUser()
        val teacherGrade = currentTeacherGrade().replace(" ", "_").lowercase()
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
                        .plus(
                            latestMissionAnalytics.take(2).map {
                                "Mission ${it.title}: ${it.completedLearners}/${it.assignedLearners} completed, ${it.stuckLearners} stalled"
                            }
                        )
                )
                aiGovernanceService.saveDraftVariant(
                    targetType = "analytics",
                    targetId = "teacher_analytics_$teacherGrade",
                    feature = "analytics_summary",
                    generatedText = summary,
                    createdBy = currentUser?.id.orEmpty(),
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION
                )
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "analytics_summary",
                    targetType = "analytics",
                    targetId = "teacher_analytics_$teacherGrade",
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION,
                    status = "success"
                )
                binding.tvAiSummary.text = summary
            } catch (e: Exception) {
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "analytics_summary",
                    targetType = "analytics",
                    targetId = "teacher_analytics_$teacherGrade",
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION,
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

    private fun currentTeacherGrade(): String {
        return SchoolStructure.normalizeGradeLevel(storageService.getUser()?.gradeLevel.orEmpty())
            .ifBlank { SchoolStructure.defaultGradeLevelForRole("teacher") }
    }
}
