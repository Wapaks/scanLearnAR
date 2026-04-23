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
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.Quarter
import com.example.scanlearn.models.QuizAttempt
import com.example.scanlearn.models.ScanAttempt
import com.example.scanlearn.models.StudentProgress
import com.example.scanlearn.models.Submission
import com.example.scanlearn.models.Unit as CurriculumUnit
import com.example.scanlearn.models.WeakTopicInsight
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.repositories.LessonRepository
import com.example.scanlearn.repositories.ProgressRepository
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.SchoolStructure
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
    private var remediationLesson: Lesson? = null
    private var remediationMission: Mission? = null
    private var studentGradeLevel: String = SchoolStructure.defaultGradeLevelForRole("student")
    private val curriculumRepository = CurriculumRepository()
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository()

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
            dbService.getUser(student.userId) { userProfile ->
                studentGradeLevel = SchoolStructure.resolveGradeLevel(
                    userProfile?.gradeLevel.orEmpty(),
                    userProfile?.role ?: "student"
                )
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
                            bindCurriculumInsight(
                                studentId = student.userId,
                                gradeLevel = SchoolStructure.resolveGradeLevel(
                                    userProfile?.gradeLevel.orEmpty(),
                                    userProfile?.role ?: "student"
                                )
                            )
                            bindMissionOutcomeInsight(
                                studentId = student.userId,
                                gradeLevel = SchoolStructure.resolveGradeLevel(
                                    userProfile?.gradeLevel.orEmpty(),
                                    userProfile?.role ?: "student"
                                )
                            )
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
        binding.btnOpenLinkedLesson.setOnClickListener { openLinkedLesson() }
        binding.btnSendTeacherNote.setOnClickListener { openTeacherNote() }
        binding.btnRelaunchRetryPath.setOnClickListener { relaunchRetryPath() }
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
        val objectIds = weakObjects.mapNotNull { it.itemId.takeIf { value -> value.isNotBlank() && value != "unknown" } }
        binding.btnAssignFollowUpMission.isEnabled = false
        curriculumRepository.getQuartersForGrade(studentGradeLevel) { quarters ->
            val quarter = quarters.firstOrNull()
            lessonRepository.getAllLessons { lessons ->
                val linkedLesson = lessons
                    .filter { quarter == null || it.quarterId == quarter.id }
                    .firstOrNull { lesson -> lesson.linkedObjectIds.any { it in objectIds } }
                    ?: lessons.firstOrNull { quarter == null || it.quarterId == quarter.id }

                val missionId = "mission_" + student.userId + "_" + System.currentTimeMillis()
                val mission = Mission(
                    id = missionId,
                    title = "${student.name} - $categoryLabel Reinforcement",
                    description = buildString {
                        append("Follow-up mission created for ${student.name} to review low-scoring topics in $categoryLabel.")
                        if (linkedLesson != null) {
                            append(" Recommended lesson: ${linkedLesson.title}.")
                        }
                    },
                    missionType = "intervention_follow_up",
                    gradeLevel = linkedLesson?.gradeLevel ?: studentGradeLevel,
                    quarterId = linkedLesson?.quarterId ?: quarter?.id.orEmpty(),
                    lessonIds = linkedLesson?.id?.let { listOf(it) } ?: emptyList(),
                    objectsToFind = objectIds.ifEmpty { linkedLesson?.linkedObjectIds ?: listOf("review_required") },
                    sectionIds = listOf(SchoolStructure.resolveSectionName(student.section)),
                    releasedSectionIds = listOf(SchoolStructure.resolveSectionName(student.section)),
                    category = categoryLabel.lowercase(),
                    active = true,
                    createdBy = teacher?.id ?: "",
                    createdAt = timestamp,
                    updatedAt = timestamp,
                    recommendedForStudentId = student.userId
                )

                dbService.saveMission(mission) { success ->
                    runOnUiThread {
                        binding.btnAssignFollowUpMission.isEnabled = true
                        if (success) {
                            showFollowUpMissionDraft()
                            Toast.makeText(
                                this,
                                "Follow-up mission aligned to the current curriculum path.",
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
    }

    private fun bindCurriculumInsight(studentId: String, gradeLevel: String) {
        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val quarter = quarters.firstOrNull()
            if (quarter == null) {
                runOnUiThread {
                    binding.tvCurriculumSummary.text = "No active quarter found for this student yet."
                    binding.tvMasterySummary.text = "Mastery records are not available yet."
                }
                return@getQuartersForGrade
            }

            curriculumRepository.getUnitsForQuarter(quarter.id) { units ->
                progressRepository.getStudentLessonProgressMap(studentId) { progressMap ->
                    progressRepository.getMasteryRecords(studentId) { masteryRecords ->
                        loadTotalLessons(quarter, units, progressMap.size) { totalLessons ->
                            val completedLessons = progressMap.values.count { it.status == "completed" }
                            val mastered = masteryRecords.values.count { it.masteryStatus == "mastered" }
                            val developing = masteryRecords.values.count { it.masteryStatus != "mastered" }
                            runOnUiThread {
                                binding.tvCurriculumSummary.text =
                                    "${quarter.title}: $completedLessons of $totalLessons lessons complete."
                                binding.tvMasterySummary.text =
                                    "Mastered competencies: $mastered. Developing or not yet mastered: $developing."
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindMissionOutcomeInsight(studentId: String, gradeLevel: String) {
        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val quarter = quarters.firstOrNull()
            dbService.getAllMissions { missions ->
                dbService.getStudentMissionProgressMap(studentId) { missionProgressMap ->
                    lessonRepository.getAllLessons { lessons ->
                        val activeQuarterMissions = missions.filter { mission ->
                            mission.active &&
                                (mission.gradeLevel.isBlank() || mission.gradeLevel.equals(gradeLevel, ignoreCase = true)) &&
                                (quarter == null || mission.quarterId.isBlank() || mission.quarterId == quarter.id)
                        }
                        val completed = activeQuarterMissions.count { missionProgressMap[it.id]?.completed == true }
                        val stalledMission = activeQuarterMissions.firstOrNull {
                            val progress = missionProgressMap[it.id]
                            progress != null && !progress.completed && progress.progressPercent in 1..74
                        }
                        val nextMission = activeQuarterMissions.firstOrNull {
                            missionProgressMap[it.id] == null || missionProgressMap[it.id]?.completed == false
                        }
                        val recommendedLesson = lessons.firstOrNull { lesson ->
                            lesson.id in (stalledMission?.lessonIds ?: nextMission?.lessonIds.orEmpty())
                        }
                        remediationMission = stalledMission ?: nextMission
                        remediationLesson = recommendedLesson

                        runOnUiThread {
                            binding.tvMissionSummary.text = if (activeQuarterMissions.isEmpty()) {
                                "No lesson-linked missions are active for this learner yet."
                            } else {
                                "$completed of ${activeQuarterMissions.size} quarter missions completed. ${activeQuarterMissions.size - completed} still need teacher follow-through or learner retry."
                            }
                            binding.tvRemediationSummary.text = when {
                                stalledMission != null && recommendedLesson != null ->
                                    "Stalled mission detected: ${stalledMission.title}. Recommended next step: revisit ${recommendedLesson.title} and then relaunch the mission."
                                nextMission != null && recommendedLesson != null ->
                                    "Next curriculum move: guide the learner through ${recommendedLesson.title}, then continue with ${nextMission.title}."
                                else ->
                                    "No urgent remediation signal yet. Keep monitoring lesson performance and mission completion."
                            }
                            binding.btnOpenLinkedLesson.isEnabled = remediationLesson != null
                            binding.btnRelaunchRetryPath.isEnabled = remediationLesson != null || remediationMission != null
                        }
                    }
                }
            }
        }
    }

    private fun openLinkedLesson() {
        val lesson = remediationLesson
        if (lesson == null) {
            Toast.makeText(this, "No linked lesson is ready for review yet.", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(android.content.Intent(this, LessonStudioActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_LESSON_ID, lesson.id)
        })
    }

    private fun openTeacherNote() {
        startActivity(android.content.Intent(this, ChatConversationActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_CHAT_PARTNER_ID, student.userId)
            putExtra(AppConstants.EXTRA_CHAT_PARTNER_NAME, student.name)
            putExtra(AppConstants.EXTRA_CHAT_PARTNER_ROLE, "student")
        })
    }

    private fun relaunchRetryPath() {
        val teacher = storageService.getUser()
        val lesson = remediationLesson
        val baseMission = remediationMission
        if (lesson == null && baseMission == null) {
            Toast.makeText(this, "No retry path is ready yet for this learner.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRelaunchRetryPath.isEnabled = false
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        val mission = Mission(
            id = "retry_" + student.userId + "_" + System.currentTimeMillis(),
            title = "${student.name} - Retry Path",
            description = buildString {
                append("Retry path created for ${student.name} based on current mission outcome signals.")
                if (lesson != null) {
                    append(" Revisit ${lesson.title} and retry the guided mission tasks.")
                }
            },
            missionType = "retry_remediation",
            gradeLevel = lesson?.gradeLevel ?: baseMission?.gradeLevel.orEmpty().ifBlank { studentGradeLevel },
            quarterId = lesson?.quarterId ?: baseMission?.quarterId.orEmpty(),
            lessonIds = lesson?.id?.let { listOf(it) } ?: baseMission?.lessonIds.orEmpty(),
            objectsToFind = when {
                lesson != null && lesson.linkedObjectIds.isNotEmpty() -> lesson.linkedObjectIds
                baseMission != null && baseMission.objectsToFind.isNotEmpty() -> baseMission.objectsToFind
                else -> weakObjectItems.map { it.itemId }.filter { it.isNotBlank() && it != "unknown" }
            },
            sectionIds = listOf(SchoolStructure.resolveSectionName(student.section)),
            releasedSectionIds = listOf(SchoolStructure.resolveSectionName(student.section)),
            category = baseMission?.category ?: weakCategoryItems.firstOrNull()?.itemId ?: "science",
            active = true,
            createdBy = teacher?.id.orEmpty(),
            createdAt = timestamp,
            updatedAt = timestamp,
            recommendedForStudentId = student.userId
        )

        dbService.saveMission(mission) { success ->
            runOnUiThread {
                binding.btnRelaunchRetryPath.isEnabled = true
                if (success) {
                    remediationMission = mission
                    Toast.makeText(
                        this,
                        "Retry path relaunched for ${student.name}.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Could not relaunch the retry path.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadTotalLessons(
        quarter: Quarter,
        units: List<CurriculumUnit>,
        fallbackTotal: Int,
        onResult: (Int) -> Unit
    ) {
        if (units.isEmpty()) {
            onResult(fallbackTotal)
            return
        }

        val lessons = mutableListOf<Lesson>()
        var remaining = units.size
        units.forEach { unit ->
            lessonRepository.getLessonsForUnit(unit.id) { unitLessons ->
                synchronized(lessons) {
                    lessons.addAll(unitLessons.filter { it.quarterId == quarter.id })
                    remaining -= 1
                    if (remaining == 0) {
                        onResult(lessons.distinctBy { it.id }.size.coerceAtLeast(fallbackTotal))
                    }
                }
            }
        }
    }

}
