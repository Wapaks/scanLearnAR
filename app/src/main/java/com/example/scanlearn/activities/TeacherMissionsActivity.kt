package com.example.scanlearn.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.MissionAdapter
import com.example.scanlearn.databinding.ActivityTeacherMissionsBinding
import com.example.scanlearn.models.Mission
import com.example.scanlearn.services.RealtimeDbService

class TeacherMissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherMissionsBinding
    private lateinit var dbService: RealtimeDbService
    private var allMissions: List<Mission> = emptyList()
    private var currentFilter = FILTER_ACTIVE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherMissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()

        binding.btnBack.setOnClickListener { finish() }
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

        binding.rvMissions.adapter = MissionAdapter(filtered) { }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvMissions.visibility = if (isLoading) View.GONE else binding.rvMissions.visibility
        binding.emptyState.visibility = if (isLoading) View.GONE else binding.emptyState.visibility
    }

    companion object {
        private const val FILTER_ACTIVE = "active"
        private const val FILTER_ARCHIVED = "archived"
    }
}
