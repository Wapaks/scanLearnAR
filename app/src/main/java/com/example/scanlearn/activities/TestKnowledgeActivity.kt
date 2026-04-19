package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.LessonOutlineAdapter
import com.example.scanlearn.databinding.ActivityTestKnowledgeBinding
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.StudentLessonProgress
import com.example.scanlearn.models.Unit as CurriculumUnit
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.repositories.LessonRepository
import com.example.scanlearn.repositories.ProgressRepository
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants

class TestKnowledgeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestKnowledgeBinding
    private lateinit var storage: StorageService
    private val curriculumRepository = CurriculumRepository()
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestKnowledgeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        binding.btnBack.setOnClickListener { finish() }

        loadLessonChoices()
    }

    private fun loadLessonChoices() {
        val user = storage.getUser() ?: return
        val gradeLevel = user.gradeLevel.ifBlank { "Grade 3" }

        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val activeQuarter = quarters.firstOrNull() ?: return@getQuartersForGrade
            curriculumRepository.getUnitsForQuarter(activeQuarter.id) { units ->
                progressRepository.getStudentLessonProgressMap(user.id) { progressMap ->
                    collectLessons(units, progressMap)
                }
            }
        }
    }

    private fun collectLessons(
        units: List<CurriculumUnit>,
        progressMap: Map<String, StudentLessonProgress>
    ) {
        val lessons = mutableListOf<Lesson>()
        if (units.isEmpty()) {
            renderLessons(lessons, progressMap)
            return
        }

        var remaining = units.size
        units.forEach { unit ->
            lessonRepository.getLessonsForUnit(unit.id) { unitLessons ->
                synchronized(lessons) {
                    lessons.addAll(unitLessons)
                    remaining -= 1
                    if (remaining == 0) {
                        renderLessons(lessons.sortedBy { it.orderIndex }, progressMap)
                    }
                }
            }
        }
    }

    private fun renderLessons(
        lessons: List<Lesson>,
        progressMap: Map<String, StudentLessonProgress>
    ) {
        runOnUiThread {
            binding.rvLessons.layoutManager = LinearLayoutManager(this)
            binding.rvLessons.adapter = LessonOutlineAdapter(
                lessons = lessons,
                progressMap = progressMap,
                actionLabel = "Take Quiz"
            ) { lesson ->
                startActivity(Intent(this, LessonPlayerActivity::class.java).apply {
                    putExtra(AppConstants.EXTRA_LESSON_ID, lesson.id)
                    putExtra(AppConstants.EXTRA_ASSESSMENT_ONLY, true)
                })
            }
        }
    }
}
