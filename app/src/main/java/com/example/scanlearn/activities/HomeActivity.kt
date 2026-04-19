package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityHomeBinding
import com.example.scanlearn.models.User
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var storage: StorageService
    private lateinit var authService: FirebaseAuthService
    private lateinit var dbService: RealtimeDbService
    private var currentUser: User? = null
    private var conversationsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        authService = FirebaseAuthService()
        dbService = RealtimeDbService()

        val user = storage.getUser()
        currentUser = user
        binding.tvWelcome.text = "Welcome, ${user?.name ?: "Student"}!"
        binding.fabChat.text = "Chat"
        binding.tvLearningPlanTitle.text = user?.gradeLevel?.takeIf { it.isNotBlank() }?.let {
            "$it Learning Plan"
        } ?: "My Learning Plan"

        binding.btnLearningPlan.setOnClickListener {
            startActivity(Intent(this, MyLearningPlanActivity::class.java))
        }
        binding.btnExplorer.setOnClickListener {
            launchScanner(AppConstants.MODE_EXPLORER)
        }
        binding.btnMission.setOnClickListener {
            startActivity(Intent(this, MissionsActivity::class.java))
        }
        binding.btnChallenge.setOnClickListener {
            startActivity(Intent(this, TestKnowledgeActivity::class.java))
        }
        binding.btnProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }
        binding.fabChat.setOnClickListener {
            startActivity(Intent(this, ChatInboxActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            authService.signOut()
            storage.clearUser()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        observeUnreadChats()
    }

    override fun onStop() {
        super.onStop()
        dbService.removeConversationsListener(conversationsListener)
        conversationsListener = null
    }

    private fun launchScanner(mode: String) {
        val intent = Intent(this, ScannerActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_MODE, mode)
        startActivity(intent)
    }

    private fun observeUnreadChats() {
        val user = currentUser ?: return
        dbService.removeConversationsListener(conversationsListener)
        conversationsListener = dbService.observeConversationsForUser(user.id) { conversations ->
            runOnUiThread {
                val unreadCount = dbService.getTotalUnreadMessages(conversations, user.id)
                binding.fabChat.text = if (unreadCount > 0) {
                    "Chat $unreadCount"
                } else {
                    "Chat"
                }
            }
        }
    }
}
