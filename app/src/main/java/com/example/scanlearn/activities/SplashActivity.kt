package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.services.DatabaseSeeder
import com.example.scanlearn.services.RealtimeDbService
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val seeder = DatabaseSeeder(RealtimeDbService())
        seeder.seed {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
            }
            finish()
        }
    }
}