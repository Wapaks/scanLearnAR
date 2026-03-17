package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityAuthBinding
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.StorageService

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var authService: FirebaseAuthService
    private lateinit var storage: StorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authService = FirebaseAuthService()
        storage = StorageService(this)

        binding.btnSignIn.setOnClickListener { handleLogin() }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate inputs
        var hasError = false

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            hasError = true
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            hasError = true
        } else {
            binding.tilPassword.error = null
        }

        if (hasError) return

        setLoading(true)

        authService.login(
            email = email,
            password = password,
            onSuccess = { user ->
                storage.saveUser(user)
                setLoading(false)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            },
            onError = { errorMessage ->
                setLoading(false)
                binding.tilPassword.error = errorMessage
            }
        )
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSignIn.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignIn.text = if (isLoading) "" else "Sign In"
    }
}