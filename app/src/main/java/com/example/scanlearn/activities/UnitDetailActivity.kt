package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.LessonOutlineAdapter
import com.example.scanlearn.databinding.ActivityUnitDetailBinding
import com.example.scanlearn.models.StudentLessonProgress
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.repositories.LessonRepository
import com.example.scanlearn.repositories.ProgressRepository
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants

class UnitDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnitDetailBinding
    private lateinit var storage: StorageService
    private val curriculumRepository = CurriculumRepository()
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }
        loadUnit()
    }

    private fun loadUnit() {
        val unitId = intent.getStringExtra(AppConstants.EXTRA_UNIT_ID).orEmpty()
        val studentId = storage.getUser()?.id.orEmpty()
        if (unitId.isBlank() || studentId.isBlank()) return

        curriculumRepository.getUnit(unitId) { unit ->
            if (unit == null) {
                runOnUiThread {
                    Toast.makeText(this, "Could not load this unit.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@getUnit
            }

            lessonRepository.getLessonsForUnit(unit.id) { lessons ->
                progressRepository.getStudentLessonProgressMap(studentId) { progressMap ->
                    runOnUiThread {
                        binding.tvUnitTitle.text = unit.title
                        binding.tvUnitOverview.text = unit.overview
                        binding.tvUnitMeta.text = "${lessons.size} lessons • ${unit.estimatedMinutes} minutes"

                        binding.rvLessons.layoutManager = LinearLayoutManager(this)
                        binding.rvLessons.adapter = LessonOutlineAdapter(
                            lessons = lessons,
                            progressMap = progressMap,
                            actionLabel = "Start"
                        ) { lesson ->
                            openLesson(lesson.id, false)
                        }

                        val nextLesson = lessons.firstOrNull { progressMap[it.id]?.status != "completed" }
                            ?: lessons.firstOrNull()
                        binding.btnStartUnit.setOnClickListener {
                            if (nextLesson != null) {
                                openLesson(nextLesson.id, false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openLesson(lessonId: String, assessmentOnly: Boolean) {
        startActivity(Intent(this, LessonPlayerActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_LESSON_ID, lessonId)
            putExtra(AppConstants.EXTRA_ASSESSMENT_ONLY, assessmentOnly)
        })
    }
}
