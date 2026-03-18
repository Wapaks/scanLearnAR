package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityRegisterBinding
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.StorageService

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authService: FirebaseAuthService
    private lateinit var storage: StorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authService = FirebaseAuthService()
        storage = StorageService(this)

        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbTeacher.id) {
                binding.sectionGroup.visibility = View.GONE
                binding.tilStudentNumber.visibility = View.GONE
            } else {
                binding.sectionGroup.visibility = View.VISIBLE
                binding.tilStudentNumber.visibility = View.VISIBLE
            }
        }

        binding.btnRegister.setOnClickListener { handleRegister() }
        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    private fun handleRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        val isTeacher = binding.rbTeacher.isChecked
        val role = if (isTeacher) "teacher" else "student"
        val studentNumber = if (!isTeacher) binding.etStudentNumber.text.toString().trim() else ""

        val section = when {
            !isTeacher && binding.rbSantan.isChecked -> "Santan"
            !isTeacher && binding.rbDaisy.isChecked -> "Daisy"
            !isTeacher && binding.rbOrchid.isChecked -> "Orchid"
            else -> ""
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

        if (!isTeacher && section.isEmpty()) {
            binding.tvSectionError.visibility = View.VISIBLE
            hasError = true
        } else {
            binding.tvSectionError.visibility = View.GONE
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
            onSuccess = { user ->
                storage.saveUser(user)
                setLoading(false)
                if (user.role == "teacher") {
                    val intent = Intent(this, TeacherActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
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