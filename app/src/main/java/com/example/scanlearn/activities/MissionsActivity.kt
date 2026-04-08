package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.MissionAdapter
import com.example.scanlearn.databinding.ActivityMissionsBinding
import com.example.scanlearn.models.Mission
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants

class MissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMissionsBinding
    private lateinit var storage: StorageService
    private lateinit var dbService: RealtimeDbService
    private var missions: List<Mission> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        dbService = RealtimeDbService()

        binding.btnBack.setOnClickListener { finish() }

        loadMissions()
    }

    private fun loadMissions() {
        val user = storage.getUser()
        if (user == null) {
            finish()
            return
        }
        val section = user?.section ?: ""

        dbService.getMissionsForSection(section) { firebaseMissions ->
            dbService.getStudentMissionProgressMap(user.id) { progressMap ->
                runOnUiThread {
                    missions = firebaseMissions.map { mission ->
                        val progress = progressMap[mission.id]
                        mission.copy(
                            completed = progress?.completed ?: false,
                            progressPercent = progress?.progressPercent ?: 0,
                            completedObjectIds = progress?.completedObjectIds ?: emptyList()
                        )
                    }

                    binding.rvMissions.layoutManager = LinearLayoutManager(this)
                    binding.rvMissions.adapter = MissionAdapter(missions) { mission ->
                        val intent = Intent(this, ScannerActivity::class.java)
                        intent.putExtra(AppConstants.EXTRA_MODE, AppConstants.MODE_MISSION)
                        intent.putExtra(AppConstants.EXTRA_MISSION_ID, mission.id)
                        intent.putExtra(AppConstants.EXTRA_MISSION_TITLE, mission.title)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}
