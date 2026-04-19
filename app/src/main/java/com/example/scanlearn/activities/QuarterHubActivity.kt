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
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import android.content.Intent

class QuarterHubActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuarterHubBinding
    private lateinit var storage: StorageService
    private val curriculumRepository = CurriculumRepository()
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository()

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

        loadQuarter()
    }

    private fun loadQuarter() {
        val quarterId = intent.getStringExtra(AppConstants.EXTRA_QUARTER_ID).orEmpty()
        val currentUser = storage.getUser()
        val studentId = currentUser?.id.orEmpty()
        if (quarterId.isBlank() || studentId.isBlank()) return

        val gradeLevel = currentUser?.gradeLevel?.ifBlank { "Grade 3" } ?: "Grade 3"

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
        units.forEach { unit ->
            lessonRepository.getLessonsForUnit(unit.id) { lessons ->
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

        runOnUiThread {
            binding.tvQuarterTitle.text = quarter.title
            binding.tvQuarterDescription.text = quarter.description
            binding.tvQuarterProgress.text = "$completedLessons / ${allLessons.size} lessons done"
            binding.progressQuarter.max = totalLessons
            binding.progressQuarter.progress = completedLessons

            binding.rvUnits.layoutManager = LinearLayoutManager(this)
            binding.rvUnits.adapter = QuarterUnitAdapter(units, unitCompletion) { unit ->
                startActivity(Intent(this, UnitDetailActivity::class.java).apply {
                    putExtra(AppConstants.EXTRA_UNIT_ID, unit.id)
                    putExtra(AppConstants.EXTRA_UNIT_TITLE, unit.title)
                })
            }
        }
    }

    private fun launchScanner(mode: String) {
        startActivity(android.content.Intent(this, ScannerActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_MODE, mode)
        })
    }
}
