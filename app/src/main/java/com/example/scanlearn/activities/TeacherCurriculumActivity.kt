package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.TeacherLessonAdapter
import com.example.scanlearn.databinding.ActivityTeacherCurriculumBinding
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.Quarter
import com.example.scanlearn.models.Unit
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.repositories.LessonRepository
import com.example.scanlearn.utils.AppConstants

class TeacherCurriculumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherCurriculumBinding
    private val curriculumRepository = CurriculumRepository()
    private val lessonRepository = LessonRepository()
    private var allRows: List<TeacherLessonAdapter.LessonRow> = emptyList()
    private var currentFilter = FILTER_ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherCurriculumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAll.setOnClickListener { applyFilter(FILTER_ALL) }
        binding.btnReview.setOnClickListener { applyFilter(FILTER_REVIEW) }
        binding.btnPublished.setOnClickListener { applyFilter(FILTER_PUBLISHED) }

        loadCurriculum()
    }

    override fun onResume() {
        super.onResume()
        loadCurriculum()
    }

    private fun loadCurriculum() {
        setLoading(true)
        curriculumRepository.getQuartersForGrade("Grade 3") { quarters ->
            val quarter = quarters.firstOrNull()
            if (quarter == null) {
                runOnUiThread {
                    setLoading(false)
                    binding.tvQuarterTitle.text = "No curriculum yet"
                }
                return@getQuartersForGrade
            }

            curriculumRepository.getUnitsForQuarter(quarter.id) { units ->
                lessonRepository.getAllLessons { lessons ->
                    buildLessonRows(quarter, units, lessons)
                }
            }
        }
    }

    private fun buildLessonRows(quarter: Quarter, units: List<Unit>, lessons: List<Lesson>) {
        val unitMap = units.associateBy { it.id }
        val quarterLessons = lessons
            .filter { it.quarterId == quarter.id }
            .sortedWith(compareBy<Lesson> { unitMap[it.unitId]?.orderIndex ?: Int.MAX_VALUE }.thenBy { it.orderIndex })

        if (quarterLessons.isEmpty()) {
            runOnUiThread {
                setLoading(false)
                binding.tvQuarterTitle.text = quarter.title
                binding.tvQuarterMeta.text = "No lessons yet"
                binding.emptyState.visibility = View.VISIBLE
                binding.rvLessons.visibility = View.GONE
            }
            return
        }

        val activityCounts = mutableMapOf<String, Int>()
        var remaining = quarterLessons.size
        quarterLessons.forEach { lesson ->
            lessonRepository.getActivitiesForLesson(lesson.id) { activities ->
                synchronized(activityCounts) {
                    activityCounts[lesson.id] = activities.size
                    remaining -= 1
                    if (remaining == 0) {
                        allRows = quarterLessons.map { item ->
                            TeacherLessonAdapter.LessonRow(
                                lesson = item,
                                unitTitle = unitMap[item.unitId]?.title ?: "Unit",
                                activityCount = activityCounts[item.id] ?: 0
                            )
                        }
                        runOnUiThread {
                            binding.tvQuarterTitle.text = quarter.title
                            binding.tvQuarterMeta.text = "${units.size} units • ${quarterLessons.size} lessons"
                            setLoading(false)
                            applyFilter(currentFilter)
                        }
                    }
                }
            }
        }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        binding.btnAll.isSelected = filter == FILTER_ALL
        binding.btnReview.isSelected = filter == FILTER_REVIEW
        binding.btnPublished.isSelected = filter == FILTER_PUBLISHED

        val filtered = when (filter) {
            FILTER_REVIEW -> allRows.filter {
                it.lesson.status.equals("review", ignoreCase = true) ||
                    it.lesson.status.equals("draft", ignoreCase = true)
            }
            FILTER_PUBLISHED -> allRows.filter { it.lesson.status.equals("published", ignoreCase = true) }
            else -> allRows
        }

        binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvLessons.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        if (filtered.isNotEmpty()) {
            binding.rvLessons.layoutManager = LinearLayoutManager(this)
            binding.rvLessons.adapter = TeacherLessonAdapter(filtered) { lesson ->
                startActivity(Intent(this, LessonStudioActivity::class.java).apply {
                    putExtra(AppConstants.EXTRA_LESSON_ID, lesson.id)
                })
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    companion object {
        private const val FILTER_ALL = "all"
        private const val FILTER_REVIEW = "review"
        private const val FILTER_PUBLISHED = "published"
    }
}
