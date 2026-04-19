package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.TeacherMissionAdapter
import com.example.scanlearn.databinding.ActivityTeacherMissionsBinding
import com.example.scanlearn.models.Mission
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TeacherMissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherMissionsBinding
    private lateinit var dbService: RealtimeDbService
    private var allMissions: List<Mission> = emptyList()
    private var currentFilter = FILTER_ACTIVE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherMissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        dbService = RealtimeDbService()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddMission.setOnClickListener {
            startActivity(Intent(this, AddEditMissionActivity::class.java))
        }
        binding.tabActive.setOnClickListener { showFilter(FILTER_ACTIVE) }
        binding.tabArchived.setOnClickListener { showFilter(FILTER_ARCHIVED) }
        binding.rvMissions.layoutManager = LinearLayoutManager(this)

        loadMissions()
    }

    override fun onResume() {
        super.onResume()
        loadMissions()
    }

    private fun loadMissions() {
        setLoading(true)
        dbService.getAllMissions { missions ->
            allMissions = missions.sortedWith(
                compareByDescending<Mission> { it.active }
                    .thenBy { it.title }
            )

            runOnUiThread {
                setLoading(false)
                showFilter(currentFilter)
            }
        }
    }

    private fun showFilter(filter: String) {
        currentFilter = filter
        binding.tabActive.isSelected = filter == FILTER_ACTIVE
        binding.tabArchived.isSelected = filter == FILTER_ARCHIVED

        val filtered = when (filter) {
            FILTER_ARCHIVED -> allMissions.filter { !it.active }
            else -> allMissions.filter { it.active }
        }

        binding.tvSummary.text = "${filtered.size} mission(s)"
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
                    loadMissions()
                } else {
                    Toast.makeText(this, "Could not update mission status.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val FILTER_ACTIVE = "active"
        private const val FILTER_ARCHIVED = "archived"
    }
}
