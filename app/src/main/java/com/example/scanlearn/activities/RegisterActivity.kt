package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityRegisterBinding
import com.example.scanlearn.models.SectionRecord
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.SchoolStructure

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authService: FirebaseAuthService
    private lateinit var storage: StorageService
    private lateinit var dbService: RealtimeDbService
    private var sectionRecords: List<SectionRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authService = FirebaseAuthService()
        storage = StorageService(this)
        dbService = RealtimeDbService()

        setupGradeDropdown()
        loadSectionMasterData()

        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbTeacher.id) {
                binding.tilStudentNumber.visibility = View.GONE
                binding.tilSection.visibility = View.GONE
                binding.etSection.setText("", false)
                binding.tvSectionHelper.text = "Teachers choose the grade they handle so they only manage that grade."
                val teacherGrade = binding.etGradeLevel.text?.toString().orEmpty()
                if (teacherGrade.isBlank()) {
                    binding.etGradeLevel.setText(SchoolStructure.defaultGradeLevelForRole("teacher"), false)
                }
            } else {
                binding.tilStudentNumber.visibility = View.VISIBLE
                binding.tilSection.visibility = View.VISIBLE
                binding.tvSectionHelper.text = "Students must choose their grade and section. Teachers choose the grade they handle."
                val selectedGrade = binding.etGradeLevel.text?.toString().orEmpty()
                val studentGrade = selectedGrade.ifBlank { SchoolStructure.defaultGradeLevelForRole("student") }
                binding.etGradeLevel.setText(studentGrade, false)
                setupSectionDropdown(studentGrade)
            }
        }

        binding.btnRegister.setOnClickListener { handleRegister() }
        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    private fun setupGradeDropdown() {
        val grades = SchoolStructure.gradeLevels
        binding.etGradeLevel.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, grades)
        )
        binding.etGradeLevel.setText(SchoolStructure.defaultGradeLevelForRole("student"), false)
        binding.etGradeLevel.setOnItemClickListener { _, _, position, _ ->
            val selectedGrade = grades.getOrNull(position).orEmpty()
            binding.tilGradeLevel.error = null
            if (binding.rbStudent.isChecked) {
                setupSectionDropdown(selectedGrade)
            }
        }
    }

    private fun setupSectionDropdown(gradeLevel: String) {
        val sections = sectionRecords
            .filter { it.gradeLevel.equals(gradeLevel, ignoreCase = true) }
            .map { it.name }
            .ifEmpty { SchoolStructure.sectionsForGrade(gradeLevel) }
        binding.etSection.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sections)
        )
        binding.etSection.setText("", false)
        binding.tilSection.error = null
    }

    private fun loadSectionMasterData() {
        dbService.getAllSections { sections ->
            runOnUiThread {
                sectionRecords = sections
                setupSectionDropdown(
                    binding.etGradeLevel.text?.toString().orEmpty()
                        .ifBlank { SchoolStructure.defaultGradeLevelForRole("student") }
                )
            }
        }
    }

    private fun handleRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        val isTeacher = binding.rbTeacher.isChecked
        val role = if (isTeacher) "teacher" else "student"
        val studentNumber = if (!isTeacher) binding.etStudentNumber.text.toString().trim() else ""
        val gradeLevel = SchoolStructure.normalizeGradeLevel(binding.etGradeLevel.text.toString())
        val section = if (isTeacher) {
            ""
        } else {
            SchoolStructure.normalizeSectionName(binding.etSection.text.toString())
        }

        var hasError = false

        if (name.isEmpty()) {
            binding.tilName.error = "Full name is required"
            hasError = true
        } else {
            binding.tilName.error = null
        }

        if (!isTeacher && studentNumber.isEmpty()) {
            binding.tilStudentNumber.error = "Student number is required"
            hasError = true
        } else {
            binding.tilStudentNumber.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            hasError = true
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            hasError = true
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            hasError = true
        } else {
            binding.tilPassword.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Please confirm your password"
            hasError = true
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            hasError = true
        } else {
            binding.tilConfirmPassword.error = null
        }

        if (gradeLevel.isEmpty()) {
            binding.tilGradeLevel.error = "Grade level is required"
            hasError = true
        } else {
            binding.tilGradeLevel.error = null
        }

        if (!isTeacher && section.isEmpty()) {
            binding.tilSection.error = "Section is required"
            hasError = true
        } else if (!isTeacher && section !in SchoolStructure.sectionsForGrade(gradeLevel)) {
            binding.tilSection.error = "Choose a valid section for $gradeLevel"
            hasError = true
        } else {
            binding.tilSection.error = null
        }

        if (hasError) return

        setLoading(true)

        authService.register(
            name = name,
            email = email,
            studentNumber = studentNumber,
            password = password,
            role = role,
            section = section,
            gradeLevel = gradeLevel,
            onSuccess = { user ->
                dbService.saveUser(user) { saved ->
                    runOnUiThread {
                        setLoading(false)
                        if (!saved) {
                            binding.tilEmail.error = "Could not save your profile. Please try again."
                            return@runOnUiThread
                        }

                        storage.saveUser(user)
                        if (user.role == "teacher") {
                            val intent = Intent(this, TeacherActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            val intent = Intent(this, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
            },
            onError = { errorMessage ->
                setLoading(false)
                binding.tilEmail.error = errorMessage
            }
        )
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnRegister.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.text = if (isLoading) "" else "Create Account"
    }
}
