package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityHomeBinding
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var storage: StorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        val user = storage.getUser()

        binding.tvWelcome.text = "Welcome, ${user?.name ?: "Student"}!"

        binding.btnExplorer.setOnClickListener {
            launchScanner(AppConstants.MODE_EXPLORER)
        }
        binding.btnMission.setOnClickListener {
            startActivity(Intent(this, MissionsActivity::class.java))
        }
        binding.btnChallenge.setOnClickListener {
            launchScanner(AppConstants.MODE_CHALLENGE)
        }
        binding.btnProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }
    }

    private fun launchScanner(mode: String) {
        val intent = Intent(this, ScannerActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_MODE, mode)
        startActivity(intent)
    }
}
