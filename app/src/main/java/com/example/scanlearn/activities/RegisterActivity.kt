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

        binding.btnRegister.setOnClickListener { handleRegister() }

        binding.tvGoToLogin.setOnClickListener {
            finish() // Go back to AuthActivity
        }
    }

    private fun handleRegister() {
        val name = binding.etName.text.toString().trim()
        val studentNumber = binding.etStudentNumber.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validate all fields
        var hasError = false

        if (name.isEmpty()) {
            binding.tilName.error = "Full name is required"
            hasError = true
        } else {
            binding.tilName.error = null
        }

        if (studentNumber.isEmpty()) {
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

        if (hasError) return

        setLoading(true)

        authService.register(
            name = name,
            email = email,
            studentNumber = studentNumber,
            password = password,
            onSuccess = { user ->
                storage.saveUser(user)
                setLoading(false)
                // Go straight to home after successful registration
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
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