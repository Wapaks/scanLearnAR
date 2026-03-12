package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityAuthBinding
import com.example.scanlearn.models.User
import com.example.scanlearn.services.StorageService

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var storage: StorageService
    private var isSignUp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)

        updateFormMode()

        binding.btnSubmit.setOnClickListener { handleSubmit() }
        binding.tvSwitch.setOnClickListener {
            isSignUp = !isSignUp
            updateFormMode()
        }
    }

    private fun updateFormMode() {
        if (isSignUp) {
            binding.tilName.visibility = View.VISIBLE
            binding.tilStudentNumber.visibility = View.VISIBLE
            binding.tilEmail.hint = "Email"
            binding.btnSubmit.text = "Sign Up"
            binding.tvSwitch.text = "Already have an account? Sign In"
            binding.tvFormTitle.text = "Create Account"
        } else {
            binding.tilName.visibility = View.GONE
            binding.tilStudentNumber.visibility = View.GONE
            binding.tilEmail.hint = "Email or Student Number"
            binding.btnSubmit.text = "Sign In"
            binding.tvSwitch.text = "Don't have an account? Sign Up"
            binding.tvFormTitle.text = "Welcome Back"
        }
    }

    private fun handleSubmit() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email or Student Number is required"
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        }

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        if (isSignUp) {
            val name = binding.etName.text.toString().trim()
            val studentNumber = binding.etStudentNumber.text.toString().trim()

            if (name.isEmpty()) {
                binding.tilName.error = "Name is required"
                return
            }
            if (studentNumber.isEmpty()) {
                binding.tilStudentNumber.error = "Student Number is required"
                return
            }
            binding.tilName.error = null
            binding.tilStudentNumber.error = null

            val user = User(
                id = System.currentTimeMillis().toString(),
                name = name,
                email = email,
                studentNumber = studentNumber
            )
            storage.saveUser(user)
            storage.saveRegisteredUser(user, password)
        } else {
            // Demo mode: any credentials work
            val user = User(
                id = System.currentTimeMillis().toString(),
                name = if (email.contains("@")) email.substringBefore("@") else "Student",
                email = email,
                studentNumber = email
            )
            storage.saveUser(user)
        }

        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
