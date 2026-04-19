package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityMyLearningPlanBinding
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.Quarter
import com.example.scanlearn.models.StudentLessonProgress
import com.example.scanlearn.models.Unit as CurriculumUnit
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.repositories.LessonRepository
import com.example.scanlearn.repositories.ProgressRepository
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants

class MyLearningPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyLearningPlanBinding
    private lateinit var storage: StorageService
    private val dbService = RealtimeDbService()
    private val curriculumRepository = CurriculumRepository()
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyLearningPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnOpenQuarter.setOnClickListener { openQuarterHub() }
        binding.btnViewProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }
        binding.btnOpenMissionCenter.setOnClickListener {
            startActivity(Intent(this, MissionsActivity::class.java))
        }

        loadLearningPlan()
    }

    private fun loadLearningPlan() {
        val user = storage.getUser() ?: return
        val gradeLevel = user.gradeLevel.ifBlank { "Grade 3" }

        binding.loadingIndicator.visibility = View.VISIBLE
        binding.contentGroup.visibility = View.GONE
        binding.tvStudentName.text = user.name.ifBlank { "Student" }
        binding.tvStudentGrade.text = gradeLevel

        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val activeQuarter = quarters.firstOrNull()
            if (activeQuarter == null) {
                runOnUiThread {
                    binding.loadingIndicator.visibility = View.GONE
                    binding.contentGroup.visibility = View.VISIBLE
                    binding.tvQuarterTitle.text = "No active quarter yet"
                    binding.tvQuarterSubtitle.text = "Curriculum data is still being prepared."
                }
                return@getQuartersForGrade
            }

            curriculumRepository.getUnitsForQuarter(activeQuarter.id) { units ->
                progressRepository.getStudentLessonProgressMap(user.id) { progressMap ->
                    loadQuarterLessons(activeQuarter, units, progressMap)
                }
            }
        }
    }

    private fun loadQuarterLessons(
        quarter: Quarter,
        units: List<CurriculumUnit>,
        progressMap: Map<String, StudentLessonProgress>
    ) {
        val lessons = mutableListOf<Lesson>()
        if (units.isEmpty()) {
            renderLearningPlan(quarter, units, lessons, progressMap)
            return
        }

        var remaining = units.size
        units.forEach { unit ->
            lessonRepository.getLessonsForUnit(unit.id) { unitLessons ->
                synchronized(lessons) {
                    lessons.addAll(unitLessons)
                    remaining -= 1
                    if (remaining == 0) {
                        renderLearningPlan(
                            quarter = quarter,
                            units = units.sortedBy { it.orderIndex },
                            lessons = lessons.sortedBy { it.orderIndex },
                            progressMap = progressMap
                        )
                    }
                }
            }
        }
    }

    private fun renderLearningPlan(
        quarter: Quarter,
        units: List<CurriculumUnit>,
        lessons: List<Lesson>,
        progressMap: Map<String, StudentLessonProgress>
    ) {
        val completedLessons = lessons.count { lesson ->
            progressMap[lesson.id]?.status == "completed"
        }
        val totalLessons = lessons.size.coerceAtLeast(1)
        val progressPercent = if (lessons.isEmpty()) 0 else (completedLessons * 100) / totalLessons
        val nextLesson = lessons.firstOrNull { lesson ->
            progressMap[lesson.id]?.status != "completed"
        } ?: lessons.lastOrNull()
        val user = storage.getUser()
        val section = user?.section?.ifBlank { user.sectionId }.orEmpty()

        dbService.getMissionsForSection(section) { missions ->
            runOnUiThread {
                binding.loadingIndicator.visibility = View.GONE
                binding.contentGroup.visibility = View.VISIBLE

                binding.tvQuarterTitle.text = quarter.title
                binding.tvQuarterSubtitle.text = quarter.description
                binding.tvCompletionPercent.text = "$progressPercent%"
                binding.progressQuarter.max = totalLessons
                binding.progressQuarter.progress = completedLessons
                binding.tvProgressMeta.text = "$completedLessons of ${lessons.size} lessons complete"
                binding.tvUnitCount.text = units.size.toString()
                binding.tvLessonCount.text = lessons.size.toString()
                binding.tvMissionCount.text = missions.count { it.active }.toString()
                binding.tvNextLesson.text = nextLesson?.title ?: "Your lessons will appear here soon."

                binding.btnOpenQuarter.isEnabled = true
                binding.btnOpenQuarter.tag = quarter
            }
        }
    }

    private fun openQuarterHub() {
        val quarter = binding.btnOpenQuarter.tag as? Quarter ?: return
        val intent = Intent(this, QuarterHubActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_QUARTER_ID, quarter.id)
        intent.putExtra(AppConstants.EXTRA_QUARTER_TITLE, quarter.title)
        startActivity(intent)
    }
}
