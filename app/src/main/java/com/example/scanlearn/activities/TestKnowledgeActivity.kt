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
import com.example.scanlearn.utils.SchoolStructure

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
        val gradeLevel = SchoolStructure.resolveGradeLevel(user.gradeLevel, user.role)

        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val activeQuarter = quarters.firstOrNull() ?: return@getQuartersForGrade
            lessonRepository.getReleasedLessonsForQuarter(
                activeQuarter.id,
                SchoolStructure.resolveSectionName(user.section.ifBlank { user.sectionId })
            ) { lessons ->
                progressRepository.getStudentLessonProgressMap(user.id) { progressMap ->
                    runOnUiThread {
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
