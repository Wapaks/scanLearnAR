package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.TeacherObjectAdapter
import com.example.scanlearn.databinding.ActivityTeacherObjectsBinding
import com.example.scanlearn.models.LearningObjectAnalytics
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.utils.AppConstants

class TeacherObjectsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherObjectsBinding
    private lateinit var dbService: RealtimeDbService

    private var allObjects: List<LearningObjectAnalytics> = emptyList()
    private var currentFilter = FILTER_ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherObjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddObject.setOnClickListener {
            startActivity(Intent(this, AddObjectActivity::class.java))
        }

        binding.tabAll.setOnClickListener { showFilter(FILTER_ALL) }
        binding.tabPublished.setOnClickListener { showFilter(FILTER_PUBLISHED) }
        binding.tabArchived.setOnClickListener { showFilter(FILTER_ARCHIVED) }

        binding.rvObjects.layoutManager = LinearLayoutManager(this)

        loadObjects()
    }

    override fun onResume() {
        super.onResume()
        loadObjects()
    }

    private fun loadObjects() {
        setLoading(true)
        dbService.getLearningObjects { objects ->
            dbService.getSubmissionsForAllStudents { submissionsMap ->
                dbService.getQuizAttemptsForAllStudents { quizAttemptsMap ->
                    dbService.getScanAttemptsForAllStudents { scanAttemptsMap ->
                        allObjects = dbService.buildLearningObjectAnalytics(
                            objects = objects,
                            submissionsMap = submissionsMap,
                            quizAttemptsMap = quizAttemptsMap,
                            scanAttemptsMap = scanAttemptsMap
                        ).sortedWith(
                            compareByDescending<LearningObjectAnalytics> { it.status.equals("published", ignoreCase = true) }
                                .thenBy { it.category }
                                .thenBy { it.objectName }
                        )

                        runOnUiThread {
                            setLoading(false)
                            showFilter(currentFilter)
                        }
                    }
                }
            }
        }
    }

    private fun showFilter(filter: String) {
        currentFilter = filter

        binding.tabAll.isSelected = filter == FILTER_ALL
        binding.tabPublished.isSelected = filter == FILTER_PUBLISHED
        binding.tabArchived.isSelected = filter == FILTER_ARCHIVED

        val filtered = when (filter) {
            FILTER_PUBLISHED -> allObjects.filter { it.status.equals("published", ignoreCase = true) || it.status.isBlank() }
            FILTER_ARCHIVED -> allObjects.filter { it.status.equals("archived", ignoreCase = true) }
            else -> allObjects
        }

        binding.tvSummary.text = "${filtered.size} object(s) • ${filtered.count { it.status.equals("published", ignoreCase = true) || it.status.isBlank() }} published"
        binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvObjects.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE

        binding.rvObjects.adapter = TeacherObjectAdapter(filtered) { analytics ->
            val intent = Intent(this, TeacherObjectDetailActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_TEACHER_OBJECT_ID, analytics.objectId)
            startActivity(intent)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvObjects.visibility = if (isLoading) View.GONE else binding.rvObjects.visibility
        binding.emptyState.visibility = if (isLoading) View.GONE else binding.emptyState.visibility
    }

    companion object {
        private const val FILTER_ALL = "all"
        private const val FILTER_PUBLISHED = "published"
        private const val FILTER_ARCHIVED = "archived"
    }
}
