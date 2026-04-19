package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.StudentProgressAdapter
import com.example.scanlearn.databinding.ActivityTeacherBinding
import com.example.scanlearn.models.StudentProgress
import com.example.scanlearn.models.User
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import com.google.firebase.database.ValueEventListener

class TeacherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherBinding
    private lateinit var storage: StorageService
    private lateinit var authService: FirebaseAuthService
    private lateinit var dbService: RealtimeDbService
    private var allStudents: List<StudentProgress> = emptyList()
    private var currentSection = "Santan"
    private var currentUser: User? = null
    private var conversationsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        authService = FirebaseAuthService()
        dbService = RealtimeDbService()

        val teacher = storage.getUser()
        currentUser = teacher
        binding.tvTeacherName.text = teacher?.name ?: "Teacher"
        binding.tvTeacherEmail.text = teacher?.email ?: ""
        binding.fabChat.text = "Chat"

        binding.btnCurriculum.setOnClickListener {
            startActivity(Intent(this, TeacherCurriculumActivity::class.java))
        }
        binding.btnObjects.setOnClickListener {
            startActivity(Intent(this, TeacherObjectsActivity::class.java))
        }

        binding.btnMissions.setOnClickListener {
            startActivity(Intent(this, TeacherMissionsActivity::class.java))
        }

        binding.btnStudents.setOnClickListener {
            startActivity(Intent(this, TeacherStudentsActivity::class.java))
        }

        binding.btnAnalytics.setOnClickListener {
            startActivity(Intent(this, TeacherAnalyticsActivity::class.java))
        }

        binding.fabChat.setOnClickListener {
            startActivity(Intent(this, ChatInboxActivity::class.java))
        }

        binding.btnSignOut.setOnClickListener {
            authService.signOut()
            storage.clearUser()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        loadOverview()
    }

    override fun onResume() {
        super.onResume()
        loadOverview()
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

    private fun loadOverview() {
        setLoadingState(true)

        dbService.getAllStudents { students ->
            dbService.getLearningObjects { objects ->
                dbService.getAllMissions { missions ->
                    dbService.getQuizAttemptsForAllStudents { quizAttemptsMap ->
                        val quizAttempts = quizAttemptsMap.values.flatten()
                        val averageQuizPercent = if (quizAttempts.isEmpty()) 0 else {
                            quizAttempts.sumOf { attempt ->
                                if (attempt.totalQuestions == 0) 0 else (attempt.score * 100) / attempt.totalQuestions
                            } / quizAttempts.size
                        }
                        val publishedObjects = objects.count {
                            it.status.isBlank() || it.status.equals("published", ignoreCase = true)
                        }
                        val activeMissions = missions.count { it.active }

                        runOnUiThread {
                            setLoadingState(false)
                            binding.tvStudentsSummary.text = "${students.size} students"
                            binding.tvStudentsSummaryHint.text =
                                "$averageQuizPercent% average quiz performance across all recorded attempts."
                            binding.tvObjectsSummary.text = "${objects.size} objects"
                            binding.tvObjectsSummaryHint.text =
                                "$publishedObjects published and ${objects.size - publishedObjects} archived."
                            binding.tvMissionsSummary.text = "${missions.size} missions"
                            binding.tvMissionsSummaryHint.text =
                                "$activeMissions active mission(s) ready for students."
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
        binding.tvSectionTitle.text = "$section Section — ${filtered.size} student(s)"

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
        binding.overviewContent.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun openStudentDetail(student: StudentProgress) {
        val intent = Intent(this, StudentDetailActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_STUDENT_PROGRESS, student)
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
