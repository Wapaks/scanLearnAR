package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.StudentProgressAdapter
import com.example.scanlearn.databinding.ActivityTeacherBinding
import com.example.scanlearn.models.StudentProgress
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService

class TeacherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherBinding
    private lateinit var storage: StorageService
    private lateinit var authService: FirebaseAuthService
    private lateinit var dbService: RealtimeDbService

    private var allStudents: List<StudentProgress> = emptyList()
    private var currentSection = "Santan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        authService = FirebaseAuthService()
        dbService = RealtimeDbService()

        val teacher = storage.getUser()
        binding.tvTeacherName.text = teacher?.name ?: "Teacher"
        binding.tvTeacherEmail.text = teacher?.email ?: ""

        binding.tabSantan.setOnClickListener { showSection("Santan") }
        binding.tabDaisy.setOnClickListener { showSection("Daisy") }
        binding.tabOrchid.setOnClickListener { showSection("Orchid") }

        binding.btnAddObject.setOnClickListener { }

        binding.btnSignOut.setOnClickListener {
            authService.signOut()
            storage.clearUser()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        loadDashboard()
    }

    private fun loadDashboard() {
        setLoadingState(true)

        dbService.getAllStudents { students ->
            dbService.getSubmissionsForAllStudents { submissionsMap ->
                dbService.getScannedCountForAllStudents { scannedMap ->
                    allStudents = dbService.buildStudentProgressList(students, submissionsMap, scannedMap)
                    runOnUiThread {
                        setLoadingState(false)
                        showSection(currentSection)
                    }
                }
            }
        }
    }

    private fun showSection(section: String) {
        currentSection = section

        binding.tabSantan.isSelected = section == "Santan"
        binding.tabDaisy.isSelected = section == "Daisy"
        binding.tabOrchid.isSelected = section == "Orchid"

        val filtered = allStudents.filter { it.section == section }

        binding.tvSectionTitle.text = "$section Section — ${filtered.size} student(s)"

        if (filtered.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvStudents.visibility = View.GONE
            binding.tvEmptySection.text = "No students in $section yet."
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvStudents.visibility = View.VISIBLE
            binding.rvStudents.layoutManager = LinearLayoutManager(this)
            binding.rvStudents.adapter = StudentProgressAdapter(filtered)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvStudents.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}