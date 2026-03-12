package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.SubmissionAdapter
import com.example.scanlearn.databinding.ActivityProgressBinding
import com.example.scanlearn.services.StorageService

class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private lateinit var storage: StorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSignOut.setOnClickListener {
            storage.clearUser()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        loadProgress()
    }

    private fun loadProgress() {
        val user = storage.getUser()
        val submissions = storage.getSubmissions()
        val scanned = storage.getScannedObjects()

        binding.tvUserName.text = user?.name ?: "Student"
        binding.tvUserEmail.text = user?.email ?: ""
        binding.tvScannedCount.text = scanned.size.toString()
        binding.tvSubmissionsCount.text = submissions.size.toString()

        if (submissions.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvSubmissions.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvSubmissions.visibility = View.VISIBLE
            binding.rvSubmissions.layoutManager = LinearLayoutManager(this)
            binding.rvSubmissions.adapter = SubmissionAdapter(submissions.reversed())
        }
    }
}
