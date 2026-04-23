package com.example.scanlearn.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.QuarterUnitAdapter
import com.example.scanlearn.databinding.ActivityQuarterHubBinding
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
import com.example.scanlearn.utils.SchoolStructure
import android.content.Intent

class QuarterHubActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuarterHubBinding
    private lateinit var storage: StorageService
    private val curriculumRepository = CurriculumRepository()
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository()
    private val dbService = RealtimeDbService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuarterHubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnExplorer.setOnClickListener {
            launchScanner(AppConstants.MODE_EXPLORER)
        }
        binding.btnTestKnowledge.setOnClickListener {
            startActivity(Intent(this, TestKnowledgeActivity::class.java))
        }
        binding.btnMissionCenter.setOnClickListener {
            val quarterId = intent.getStringExtra(AppConstants.EXTRA_QUARTER_ID).orEmpty()
            val quarterTitle = intent.getStringExtra(AppConstants.EXTRA_QUARTER_TITLE).orEmpty()
            startActivity(Intent(this, MissionsActivity::class.java).apply {
                putExtra(AppConstants.EXTRA_QUARTER_ID, quarterId)
                putExtra(AppConstants.EXTRA_QUARTER_TITLE, quarterTitle)
            })
        }

        loadQuarter()
    }

    private fun loadQuarter() {
        val quarterId = intent.getStringExtra(AppConstants.EXTRA_QUARTER_ID).orEmpty()
        val currentUser = storage.getUser()
        val studentId = currentUser?.id.orEmpty()
        if (quarterId.isBlank() || studentId.isBlank()) return

        val gradeLevel = SchoolStructure.resolveGradeLevel(currentUser?.gradeLevel.orEmpty(), currentUser?.role ?: "student")

        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val quarter = quarters.firstOrNull { it.id == quarterId }
            if (quarter == null) {
                runOnUiThread {
                    Toast.makeText(this, "Could not load this quarter.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@getQuartersForGrade
            }

            curriculumRepository.getUnitsForQuarter(quarterId) { units ->
                progressRepository.getStudentLessonProgressMap(studentId) { progressMap ->
                    loadLessonsForUnits(quarter, units, progressMap)
                }
            }
        }
    }

    private fun loadLessonsForUnits(
        quarter: Quarter,
        units: List<CurriculumUnit>,
        progressMap: Map<String, StudentLessonProgress>
    ) {
        val lessonsByUnit = mutableMapOf<String, List<Lesson>>()
        if (units.isEmpty()) {
            renderQuarterHub(quarter, units, lessonsByUnit, progressMap)
            return
        }

        var remaining = units.size
        val section = SchoolStructure.resolveSectionName(
            storage.getUser()?.section?.ifBlank { storage.getUser()?.sectionId }.orEmpty()
        )
        units.forEach { unit ->
            lessonRepository.getReleasedLessonsForUnit(unit.id, section) { lessons ->
                synchronized(lessonsByUnit) {
                    lessonsByUnit[unit.id] = lessons
                    remaining -= 1
                    if (remaining == 0) {
                        renderQuarterHub(quarter, units.sortedBy { it.orderIndex }, lessonsByUnit, progressMap)
                    }
                }
            }
        }
    }

    private fun renderQuarterHub(
        quarter: Quarter,
        units: List<CurriculumUnit>,
        lessonsByUnit: Map<String, List<Lesson>>,
        progressMap: Map<String, StudentLessonProgress>
    ) {
        val allLessons = lessonsByUnit.values.flatten()
        val completedLessons = allLessons.count { progressMap[it.id]?.status == "completed" }
        val totalLessons = allLessons.size.coerceAtLeast(1)
        val unitCompletion = units.associate { unit ->
            val lessons = lessonsByUnit[unit.id].orEmpty()
            unit.id to lessons.count { progressMap[it.id]?.status == "completed" }
        }
        val user = storage.getUser()
        val section = SchoolStructure.resolveSectionName(user?.section?.ifBlank { user.sectionId }.orEmpty())
        val gradeLevel = SchoolStructure.resolveGradeLevel(user?.gradeLevel.orEmpty(), user?.role ?: "student")

        dbService.getStudentMissionProgressMap(user?.id.orEmpty()) { missionProgressMap ->
            val lessonIds = allLessons.map { it.id }.toSet()
            dbService.getMissionsForQuarter(quarter.id, section, gradeLevel) { missions ->
                val alignedMissions = missions.filter { mission ->
                    mission.lessonIds.isEmpty() || mission.lessonIds.any { it in lessonIds }
                }
                val completedMissions = alignedMissions.count { missionProgressMap[it.id]?.completed == true }

                runOnUiThread {
                    binding.tvQuarterTitle.text = quarter.title
                    binding.tvQuarterDescription.text = quarter.description
                    binding.tvQuarterProgress.text = "$completedLessons / ${allLessons.size} lessons done"
                    binding.progressQuarter.max = totalLessons
                    binding.progressQuarter.progress = completedLessons
                    binding.tvMissionSummary.text = if (alignedMissions.isEmpty()) {
                        "No quarter-linked missions yet. Teachers can connect missions to lessons from the curriculum side."
                    } else {
                        "$completedMissions of ${alignedMissions.size} quarter missions completed. These missions reinforce the same lessons in this quarter."
                    }

                    binding.rvUnits.layoutManager = LinearLayoutManager(this)
                    binding.rvUnits.adapter = QuarterUnitAdapter(units, unitCompletion) { unit ->
                        startActivity(Intent(this, UnitDetailActivity::class.java).apply {
                            putExtra(AppConstants.EXTRA_UNIT_ID, unit.id)
                            putExtra(AppConstants.EXTRA_UNIT_TITLE, unit.title)
                        })
                    }
                }
            }
        }
    }

    private fun launchScanner(mode: String) {
        startActivity(android.content.Intent(this, ScannerActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_MODE, mode)
        })
    }
}
