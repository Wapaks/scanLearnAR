package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.StudentProgressAdapter
import com.example.scanlearn.databinding.ActivityTeacherStudentsBinding
import com.example.scanlearn.models.StudentProgress
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.utils.AppConstants

class TeacherStudentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherStudentsBinding
    private lateinit var dbService: RealtimeDbService

    private var allStudents: List<StudentProgress> = emptyList()
    private var currentSection = "Santan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()

        binding.btnBack.setOnClickListener { finish() }
        binding.tabSantan.setOnClickListener { showSection("Santan") }
        binding.tabDaisy.setOnClickListener { showSection("Daisy") }
        binding.tabOrchid.setOnClickListener { showSection("Orchid") }

        loadStudents()
    }

    override fun onResume() {
        super.onResume()
        loadStudents()
    }

    private fun loadStudents() {
        setLoadingState(true)

        dbService.getAllStudents { students ->
            dbService.getSubmissionsForAllStudents { submissionsMap ->
                dbService.getScannedCountForAllStudents { scannedMap ->
                    dbService.getQuizAttemptsForAllStudents { quizAttemptsMap ->
                        dbService.getScanAttemptsForAllStudents { scanAttemptsMap ->
                            allStudents = dbService.buildStudentProgressList(
                                students = students,
                                submissionsMap = submissionsMap,
                                scannedMap = scannedMap,
                                quizAttemptsMap = quizAttemptsMap,
                                scanAttemptsMap = scanAttemptsMap
                            )

                            runOnUiThread {
                                setLoadingState(false)
                                showSection(currentSection)
                            }
                        }
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
        binding.tvSectionTitle.text = "$section Section - ${filtered.size} student(s)"

        if (filtered.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvStudents.visibility = View.GONE
            binding.tvEmptySection.text = "No students in $section yet."
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvStudents.visibility = View.VISIBLE
            binding.rvStudents.layoutManager = LinearLayoutManager(this)
            binding.rvStudents.adapter = StudentProgressAdapter(filtered) { student ->
                openStudentDetail(student)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvStudents.visibility = if (isLoading) View.GONE else binding.rvStudents.visibility
        binding.emptyState.visibility = if (isLoading) View.GONE else binding.emptyState.visibility
    }

    private fun openStudentDetail(student: StudentProgress) {
        val intent = Intent(this, StudentDetailActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_STUDENT_PROGRESS, student)
        startActivity(intent)
    }
}
