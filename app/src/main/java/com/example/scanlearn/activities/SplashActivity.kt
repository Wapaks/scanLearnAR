package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.StorageService
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                // User is still logged in — go straight to Home
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                // No active session — go to Login
                startActivity(Intent(this, AuthActivity::class.java))
            }
            finish()
        }, 1000)
    }
}