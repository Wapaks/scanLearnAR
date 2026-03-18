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
import com.example.scanlearn.services.StorageService

class TeacherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherBinding
    private lateinit var storage: StorageService
    private lateinit var authService: FirebaseAuthService

    private val sections = listOf("Santan", "Daisy", "Orchid")
    private var allStudents: List<StudentProgress> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        authService = FirebaseAuthService()

        val teacher = storage.getUser()
        binding.tvTeacherName.text = teacher?.name ?: "Teacher"
        binding.tvTeacherEmail.text = teacher?.email ?: ""

        allStudents = loadStudentProgress()

        setupTabs()

        binding.tabSantan.performClick()

        binding.btnAddObject.setOnClickListener {
        }

        binding.btnSignOut.setOnClickListener {
            authService.signOut()
            storage.clearUser()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupTabs() {
        binding.tabSantan.setOnClickListener { showSection("Santan") }
        binding.tabDaisy.setOnClickListener { showSection("Daisy") }
        binding.tabOrchid.setOnClickListener { showSection("Orchid") }
    }

    private fun showSection(section: String) {
        binding.tabSantan.isSelected = section == "Santan"
        binding.tabDaisy.isSelected = section == "Daisy"
        binding.tabOrchid.isSelected = section == "Orchid"

        val filtered = allStudents.filter { it.section == section }

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

        binding.tvSectionTitle.text = "$section Section — ${filtered.size} student(s)"
    }

    private fun loadStudentProgress(): List<StudentProgress> {
        val submissions = storage.getSubmissions()
        val scanned = storage.getScannedObjects()
        val currentUser = storage.getUser() ?: return emptyList()

        return listOf(
            StudentProgress(
                userId = currentUser.id,
                name = currentUser.name,
                studentNumber = currentUser.studentNumber,
                section = currentUser.section,
                scannedCount = scanned.size,
                submissionsCount = submissions.size
            )
        )
    }
}