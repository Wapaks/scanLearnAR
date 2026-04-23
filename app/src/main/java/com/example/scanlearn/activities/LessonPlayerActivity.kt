package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.QuizOptionAdapter
import com.example.scanlearn.databinding.ActivityLessonPlayerBinding
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.LessonActivity
import com.example.scanlearn.models.MasteryRecord
import com.example.scanlearn.models.StudentLessonProgress
import com.example.scanlearn.repositories.AssessmentRepository
import com.example.scanlearn.repositories.LessonRepository
import com.example.scanlearn.repositories.ProgressRepository
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LessonPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonPlayerBinding
    private lateinit var storage: StorageService
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository()
    private val assessmentRepository = AssessmentRepository()

    private lateinit var lesson: Lesson
    private var activities: List<LessonActivity> = emptyList()
    private var progress: StudentLessonProgress? = null
    private var currentIndex = 0
    private var currentSelectedIndex = -1
    private val numericAnswers = mutableListOf<Int>()
    private val correctnessFlags = mutableListOf<Boolean>()
    private lateinit var optionAdapter: QuizOptionAdapter
    private var assessmentOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLessonPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        assessmentOnly = intent.getBooleanExtra(AppConstants.EXTRA_ASSESSMENT_ONLY, false)

        optionAdapter = QuizOptionAdapter(
            modeColor = AppColors.getModeColor(
                if (assessmentOnly) AppConstants.MODE_TEST_KNOWLEDGE else AppConstants.MODE_LEARNING_PLAN
            )
        ) { index ->
            currentSelectedIndex = index
        }

        binding.rvOptions.layoutManager = LinearLayoutManager(this)
        binding.rvOptions.adapter = optionAdapter
        binding.btnBack.setOnClickListener { finish() }
        binding.btnNext.setOnClickListener { handleNext() }
        binding.btnFinish.setOnClickListener { navigateAfterFinish() }
        binding.btnViewProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }

        loadLesson()
    }

    private fun loadLesson() {
        val lessonId = intent.getStringExtra(AppConstants.EXTRA_LESSON_ID).orEmpty()
        val studentId = storage.getUser()?.id.orEmpty()
        if (lessonId.isBlank() || studentId.isBlank()) return

        lessonRepository.getLesson(lessonId) { loadedLesson ->
            if (loadedLesson == null) {
                runOnUiThread {
                    Toast.makeText(this, "Could not load this lesson.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@getLesson
            }
            val currentUser = storage.getUser()
            val section = currentUser?.section?.ifBlank { currentUser.sectionId }.orEmpty()
            val isTeacher = currentUser?.role?.equals("teacher", ignoreCase = true) == true
            val isPublished = loadedLesson.status.equals("published", ignoreCase = true)
            val isReleased = loadedLesson.releasedSectionIds.isEmpty() ||
                loadedLesson.releasedSectionIds.any { it.equals(section, ignoreCase = true) }
            if (!isTeacher && (!isPublished || !isReleased)) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "This lesson is not currently released for your section.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                return@getLesson
            }
            lesson = loadedLesson
            lessonRepository.getActivitiesForLesson(lesson.id) { loadedActivities ->
                progressRepository.getStudentLessonProgress(studentId, lesson.id) { savedProgress ->
                    activities = loadedActivities
                    progress = savedProgress
                    runOnUiThread { renderLessonIntro() }
                }
            }
        }
    }

    private fun renderLessonIntro() {
        binding.tvLessonTitle.text = lesson.title
        binding.tvLessonObjective.text = lesson.objective
        binding.tvLessonSummary.text = lesson.summary
        binding.tvModeLabel.text = if (assessmentOnly) "Test Knowledge" else "Lesson Player"
        binding.tvLessonMeta.text = if (assessmentOnly) {
            "${lesson.estimatedMinutes} min - ${activities.size} assessment item(s)"
        } else {
            "${lesson.estimatedMinutes} min - ${activities.size} quick check(s)"
        }
        binding.contentIntro.visibility = if (assessmentOnly) View.GONE else View.VISIBLE
        binding.resultGroup.visibility = View.GONE
        binding.questionGroup.visibility = View.VISIBLE
        binding.btnFinish.text = if (assessmentOnly) "Back to Practice" else "Back to Learning Plan"
        binding.btnNext.text = if (activities.size <= 1) "Finish Lesson" else "Next"

        if (activities.isEmpty()) {
            binding.questionGroup.visibility = View.GONE
            binding.resultGroup.visibility = View.VISIBLE
            binding.tvResultTitle.text = "No activities yet"
            binding.tvResultSummary.text = "This lesson is ready, but its quick checks have not been added yet."
            binding.tvResultMeta.text = "Return to the LMS and check again after your teacher updates this lesson."
            binding.btnViewProgress.visibility = View.GONE
            return
        }

        currentIndex = 0
        numericAnswers.clear()
        correctnessFlags.clear()
        showActivity()
    }

    private fun showActivity() {
        val item = activities[currentIndex]
        binding.tvQuestionNumber.text = "Task ${currentIndex + 1} of ${activities.size}"
        binding.tvQuestionPrompt.text = item.prompt
        binding.tvQuestionHelp.text = item.instructions.ifBlank { item.content }
        binding.progressLesson.max = activities.size
        binding.progressLesson.progress = currentIndex + 1
        binding.etShortAnswer.setText("")
        currentSelectedIndex = -1

        if (item.type.equals("short_answer", ignoreCase = true)) {
            binding.rvOptions.visibility = View.GONE
            binding.etShortAnswer.visibility = View.VISIBLE
        } else {
            binding.rvOptions.visibility = View.VISIBLE
            binding.etShortAnswer.visibility = View.GONE
            optionAdapter.setOptions(item.options)
        }

        binding.btnNext.text = if (currentIndex == activities.lastIndex) {
            if (assessmentOnly) "Submit Test" else "Finish Lesson"
        } else {
            "Next"
        }
    }

    private fun handleNext() {
        val item = activities[currentIndex]
        val evaluation = evaluateAnswer(item) ?: return
        numericAnswers.add(evaluation.numericAnswer)
        correctnessFlags.add(evaluation.correct)

        if (currentIndex == activities.lastIndex) {
            completeLesson()
        } else {
            currentIndex += 1
            showActivity()
        }
    }

    private fun evaluateAnswer(item: LessonActivity): AnswerEvaluation? {
        return if (item.type.equals("short_answer", ignoreCase = true)) {
            val response = binding.etShortAnswer.text.toString().trim()
            if (response.isBlank()) {
                Toast.makeText(this, "Please enter your answer to continue.", Toast.LENGTH_SHORT).show()
                null
            } else {
                val normalizedResponse = response.lowercase()
                val normalizedExpected = item.correctAnswer.trim().lowercase()
                AnswerEvaluation(
                    numericAnswer = if (normalizedResponse == normalizedExpected) 1 else 0,
                    correct = normalizedResponse == normalizedExpected
                )
            }
        } else {
            if (currentSelectedIndex == -1) {
                Toast.makeText(this, "Please choose an answer to continue.", Toast.LENGTH_SHORT).show()
                null
            } else {
                val correctIndex = item.options.indexOfFirst {
                    it.equals(item.correctAnswer, ignoreCase = true)
                }
                AnswerEvaluation(
                    numericAnswer = currentSelectedIndex,
                    correct = currentSelectedIndex == correctIndex
                )
            }
        }
    }

    private fun completeLesson() {
        val user = storage.getUser() ?: return
        val score = correctnessFlags.count { it }
        val total = activities.size.coerceAtLeast(1)
        val percent = (score * 100) / total
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        val previousAttempts = progress?.attempts ?: 0

        val updatedProgress = StudentLessonProgress(
            lessonId = lesson.id,
            status = "completed",
            attempts = previousAttempts + 1,
            bestScore = maxOf(progress?.bestScore ?: 0, percent),
            lastScore = percent,
            masteryStatus = if (percent >= 75) "mastered" else "needs_review",
            completedActivities = activities.map { it.id },
            completedAt = timestamp,
            lastOpenedAt = timestamp
        )

        val mode = if (assessmentOnly) AppConstants.MODE_TEST_KNOWLEDGE else AppConstants.MODE_LEARNING_PLAN
        val attempt = assessmentRepository.buildLessonAssessmentAttempt(
            studentId = user.id,
            lesson = lesson,
            score = score,
            totalQuestions = total,
            answers = numericAnswers.toList(),
            mode = mode
        )

        assessmentRepository.saveQuizAttempt(user.id, attempt) {
            progressRepository.saveStudentLessonProgress(user.id, updatedProgress) { progressSaved ->
                saveMasteryRecords(user.id, percent, timestamp) {
                    runOnUiThread {
                        if (!progressSaved) {
                            Toast.makeText(this, "Could not save lesson progress.", Toast.LENGTH_SHORT).show()
                            return@runOnUiThread
                        }
                        showResult(score, total, percent)
                    }
                }
            }
        }
    }

    private fun saveMasteryRecords(
        studentId: String,
        percent: Int,
        timestamp: String,
        onComplete: () -> Unit
    ) {
        val competencyIds = lesson.competencyIds.distinct()
        if (competencyIds.isEmpty()) {
            onComplete()
            return
        }

        var remaining = competencyIds.size
        competencyIds.forEach { competencyId ->
            progressRepository.getMasteryRecords(studentId) { records ->
                val existing = records[competencyId]
                val updated = MasteryRecord(
                    competencyId = competencyId,
                    gradeLevel = lesson.gradeLevel,
                    quarterId = lesson.quarterId,
                    evidenceIds = ((existing?.evidenceIds ?: emptyList()) + lesson.id).distinct(),
                    masteryPercent = maxOf(existing?.masteryPercent ?: 0, percent),
                    masteryStatus = if (percent >= 75) "mastered" else "developing",
                    updatedAt = timestamp
                )
                progressRepository.saveMasteryRecord(studentId, updated) {
                    remaining -= 1
                    if (remaining == 0) onComplete()
                }
            }
        }
    }

    private fun showResult(score: Int, total: Int, percent: Int) {
        binding.questionGroup.visibility = View.GONE
        binding.contentIntro.visibility = View.GONE
        binding.resultGroup.visibility = View.VISIBLE
        binding.tvResultTitle.text = if (percent >= 75) "Lesson Complete" else "Good Try"
        binding.tvResultSummary.text = "You answered $score out of $total correctly."
        binding.tvResultMeta.text = if (percent >= 75) {
            "Mastery updated. You are ready to move forward."
        } else {
            "Progress saved. You can review this lesson again anytime."
        }
        binding.btnViewProgress.visibility = View.VISIBLE
        binding.btnFinish.text = if (assessmentOnly) "Back to Practice" else "Back to Learning Plan"
    }

    private fun navigateAfterFinish() {
        val destination = if (assessmentOnly) {
            Intent(this, TestKnowledgeActivity::class.java)
        } else {
            Intent(this, MyLearningPlanActivity::class.java)
        }
        destination.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(destination)
        finish()
    }

    private data class AnswerEvaluation(
        val numericAnswer: Int,
        val correct: Boolean
    )
}
