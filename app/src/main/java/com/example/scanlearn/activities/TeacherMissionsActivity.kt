package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.TeacherMissionAdapter
import com.example.scanlearn.databinding.ActivityTeacherMissionsBinding
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.Quarter
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.SchoolStructure

class TeacherMissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherMissionsBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService
    private val curriculumRepository = CurriculumRepository()
    private var allMissions: List<Mission> = emptyList()
    private var availableQuarters: List<Quarter> = emptyList()
    private var currentFilter = FILTER_ACTIVE
    private var selectedQuarterId = FILTER_ALL_QUARTERS
    private var quarterFilterInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherMissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        dbService = RealtimeDbService()
        storageService = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddMission.setOnClickListener {
            startActivity(Intent(this, AddEditMissionActivity::class.java))
        }
        binding.tabActive.setOnClickListener { showFilter(FILTER_ACTIVE) }
        binding.tabArchived.setOnClickListener { showFilter(FILTER_ARCHIVED) }
        binding.etQuarterFilter.setOnItemClickListener { _, _, position, _ ->
            selectedQuarterId = if (position == 0) {
                FILTER_ALL_QUARTERS
            } else {
                availableQuarters.getOrNull(position - 1)?.id ?: FILTER_ALL_QUARTERS
            }
            showFilter(currentFilter)
        }
        binding.rvMissions.layoutManager = LinearLayoutManager(this)

        loadDashboard()
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }

    private fun loadDashboard() {
        setLoading(true)
        val teacherGrade = currentTeacherGrade()
        curriculumRepository.getQuartersForGrade(teacherGrade) { quarters ->
            availableQuarters = quarters.sortedBy { it.orderIndex }
            dbService.getAllMissions { missions ->
                allMissions = missions
                    .filter { it.gradeLevel.equals(teacherGrade, ignoreCase = true) }
                    .sortedWith(
                        compareByDescending<Mission> { it.quarterId == selectedQuarterId }
                            .thenByDescending { it.active }
                            .thenBy { it.title }
                    )

                runOnUiThread {
                    setupQuarterFilter()
                    setLoading(false)
                    showFilter(currentFilter)
                }
            }
        }
    }

    private fun setupQuarterFilter() {
        val labels = buildList {
            add("All Quarters")
            addAll(availableQuarters.map(::buildQuarterLabel))
        }
        binding.etQuarterFilter.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                labels
            )
        )

        if (!quarterFilterInitialized && selectedQuarterId == FILTER_ALL_QUARTERS) {
            availableQuarters.maxByOrNull { it.orderIndex }?.let { latestQuarter ->
                selectedQuarterId = latestQuarter.id
            }
            quarterFilterInitialized = true
        }

        val selectedLabel = if (selectedQuarterId == FILTER_ALL_QUARTERS) {
            "All Quarters"
        } else {
            availableQuarters.firstOrNull { it.id == selectedQuarterId }?.let(::buildQuarterLabel)
                ?: "All Quarters"
        }
        if (binding.etQuarterFilter.text?.toString() != selectedLabel) {
            binding.etQuarterFilter.setText(selectedLabel, false)
        }
    }

    private fun showFilter(filter: String) {
        currentFilter = filter
        binding.tabActive.isSelected = filter == FILTER_ACTIVE
        binding.tabArchived.isSelected = filter == FILTER_ARCHIVED

        val quarterFiltered = if (selectedQuarterId == FILTER_ALL_QUARTERS) {
            allMissions
        } else {
            allMissions.filter { it.quarterId == selectedQuarterId }
        }

        val filtered = when (filter) {
            FILTER_ARCHIVED -> quarterFiltered.filter { !it.active }
            else -> quarterFiltered.filter { it.active }
        }

        val releasedCount = filtered.count { it.releasedSectionIds.isNotEmpty() }
        val quarterLabel = if (selectedQuarterId == FILTER_ALL_QUARTERS) {
            "All quarters"
        } else {
            availableQuarters.firstOrNull { it.id == selectedQuarterId }?.let(::buildQuarterLabel)
                ?: "Selected quarter"
        }
        binding.tvSummary.text = "${filtered.size} mission(s) | $releasedCount released | $quarterLabel"
        binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvMissions.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE

        binding.rvMissions.adapter = TeacherMissionAdapter(
            missions = filtered,
            onEdit = { mission ->
                val intent = Intent(this, AddEditMissionActivity::class.java)
                intent.putExtra(AppConstants.EXTRA_MISSION_ID, mission.id)
                startActivity(intent)
            },
            onToggleStatus = { mission ->
                toggleMissionStatus(mission)
            }
        )
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvMissions.visibility = if (isLoading) View.GONE else binding.rvMissions.visibility
        binding.emptyState.visibility = if (isLoading) View.GONE else binding.emptyState.visibility
    }

    private fun toggleMissionStatus(mission: Mission) {
        val updatedMission = mission.copy(active = !mission.active)
        setLoading(true)
        dbService.saveMission(updatedMission) { success ->
            runOnUiThread {
                setLoading(false)
                if (success) {
                    Toast.makeText(
                        this,
                        if (updatedMission.active) "Mission reactivated." else "Mission archived.",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadDashboard()
                } else {
                    Toast.makeText(this, "Could not update mission status.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buildQuarterLabel(quarter: Quarter): String {
        return "Quarter ${quarter.quarterNumber}: ${quarter.title}"
    }

    private fun currentTeacherGrade(): String {
        return SchoolStructure.normalizeGradeLevel(storageService.getUser()?.gradeLevel.orEmpty())
            .ifBlank { SchoolStructure.defaultGradeLevelForRole("teacher") }
    }

    companion object {
        private const val FILTER_ACTIVE = "active"
        private const val FILTER_ARCHIVED = "archived"
        private const val FILTER_ALL_QUARTERS = "__all_quarters__"
    }
}
