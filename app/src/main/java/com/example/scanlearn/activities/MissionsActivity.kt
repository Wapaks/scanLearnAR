package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.MissionAdapter
import com.example.scanlearn.databinding.ActivityMissionsBinding
import com.example.scanlearn.models.LearningData
import com.example.scanlearn.models.Mission
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants

class MissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMissionsBinding
    private lateinit var storage: StorageService
    private var missions: List<Mission> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }

        loadMissions()
    }

    private fun loadMissions() {
        val saved = storage.getMissions()
        missions = if (saved.isNotEmpty()) saved else {
            storage.saveMissions(LearningData.MISSIONS)
            LearningData.MISSIONS
        }

        binding.rvMissions.layoutManager = LinearLayoutManager(this)
        binding.rvMissions.adapter = MissionAdapter(missions) {
            val intent = Intent(this, ScannerActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_MODE, AppConstants.MODE_MISSION)
            startActivity(intent)
        }
    }
}
