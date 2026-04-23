package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.SubmissionAdapter
import com.example.scanlearn.databinding.ActivityProgressBinding
import com.example.scanlearn.models.QuizAttempt
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.Quarter
import com.example.scanlearn.models.Unit as CurriculumUnit
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.repositories.LessonRepository
import com.example.scanlearn.repositories.ProgressRepository
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.SchoolStructure

class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private lateinit var storage: StorageService
    private lateinit var dbService: RealtimeDbService
    private lateinit var authService: FirebaseAuthService
    private val curriculumRepository = CurriculumRepository()
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository()

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
        binding.tvQuarterProgress.text = "Loading curriculum progress..."
        binding.tvLessonCompletion.text = "Lessons completed: ..."
        binding.tvMasteryProgress.text = "Mastered competencies: ..."

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

        loadCurriculumProgress(user.id, SchoolStructure.resolveGradeLevel(user.gradeLevel, user.role))
    }

    private fun loadCurriculumProgress(studentId: String, gradeLevel: String) {
        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val activeQuarter = quarters.firstOrNull()
            if (activeQuarter == null) {
                runOnUiThread {
                    binding.tvQuarterProgress.text = "No active quarter found yet."
                    binding.tvLessonCompletion.text = "Lessons completed: 0"
                    binding.tvMasteryProgress.text = "Mastered competencies: 0"
                }
                return@getQuartersForGrade
            }

            curriculumRepository.getUnitsForQuarter(activeQuarter.id) { units ->
                progressRepository.getStudentLessonProgressMap(studentId) { progressMap ->
                    progressRepository.getMasteryRecords(studentId) { masteryRecords ->
                        loadQuarterLessons(
                            quarter = activeQuarter,
                            units = units,
                            fallbackTotal = progressMap.size,
                            section = SchoolStructure.resolveSectionName(
                                storage.getUser()?.section?.ifBlank { storage.getUser()?.sectionId }.orEmpty()
                            )
                        ) { totalLessons ->
                            val completedLessons = progressMap.values.count { it.status == "completed" }
                            val mastered = masteryRecords.values.count { it.masteryStatus == "mastered" }
                            val totalMastery = masteryRecords.size

                            runOnUiThread {
                                binding.tvQuarterProgress.text =
                                    "${activeQuarter.title}: $completedLessons of $totalLessons lessons complete"
                                binding.tvLessonCompletion.text =
                                    "Lessons completed: $completedLessons of $totalLessons"
                                binding.tvMasteryProgress.text =
                                    "Mastered competencies: $mastered of $totalMastery recorded"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadQuarterLessons(
        quarter: Quarter,
        units: List<CurriculumUnit>,
        fallbackTotal: Int,
        section: String,
        onResult: (Int) -> Unit
    ) {
        if (units.isEmpty()) {
            onResult(fallbackTotal)
            return
        }

        val lessons = mutableListOf<Lesson>()
        var remaining = units.size
        units.forEach { unit ->
            lessonRepository.getReleasedLessonsForUnit(unit.id, section) { unitLessons ->
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

    private fun buildAverageScoreLabel(quizAttempts: List<QuizAttempt>): String {
        if (quizAttempts.isEmpty()) return "0%"
        val totalPercent = quizAttempts.sumOf { attempt ->
            if (attempt.totalQuestions == 0) 0 else (attempt.score * 100) / attempt.totalQuestions
        }
        return "${totalPercent / quizAttempts.size}%"
    }
}
