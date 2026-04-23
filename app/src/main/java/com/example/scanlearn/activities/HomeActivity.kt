package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityHomeBinding
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.StudentLessonProgress
import com.example.scanlearn.models.StudentMissionProgress
import com.example.scanlearn.models.User
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.SchoolStructure
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var storage: StorageService
    private lateinit var authService: FirebaseAuthService
    private lateinit var dbService: RealtimeDbService
    private val curriculumRepository = CurriculumRepository()
    private var currentUser: User? = null
    private var conversationsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        authService = FirebaseAuthService()
        dbService = RealtimeDbService()

        currentUser = storage.getUser()
        binding.fabChat.text = "Chat"
        bindHeader(currentUser)

        binding.btnLearningPlan.setOnClickListener {
            startActivity(Intent(this, MyLearningPlanActivity::class.java))
        }
        binding.btnExplorer.setOnClickListener {
            openQuarterHub()
        }
        binding.btnMission.setOnClickListener {
            openQuarterMissions()
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

        loadDashboardSnapshot()
    }

    override fun onStart() {
        super.onStart()
        observeUnreadChats()
    }

    override fun onResume() {
        super.onResume()
        currentUser = storage.getUser()
        bindHeader(currentUser)
        loadDashboardSnapshot()
    }

    override fun onStop() {
        super.onStop()
        dbService.removeConversationsListener(conversationsListener)
        conversationsListener = null
    }

    private fun openQuarterHub() {
        val user = currentUser ?: return
        val gradeLevel = SchoolStructure.resolveGradeLevel(user.gradeLevel, user.role)
        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val quarter = quarters.firstOrNull()
            runOnUiThread {
                if (quarter == null) {
                    startActivity(Intent(this, MyLearningPlanActivity::class.java))
                } else {
                    startActivity(Intent(this, QuarterHubActivity::class.java).apply {
                        putExtra(AppConstants.EXTRA_QUARTER_ID, quarter.id)
                        putExtra(AppConstants.EXTRA_QUARTER_TITLE, quarter.title)
                    })
                }
            }
        }
    }

    private fun openQuarterMissions() {
        val user = currentUser ?: return
        val gradeLevel = SchoolStructure.resolveGradeLevel(user.gradeLevel, user.role)
        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val quarter = quarters.firstOrNull()
            runOnUiThread {
                startActivity(Intent(this, MissionsActivity::class.java).apply {
                    putExtra(AppConstants.EXTRA_QUARTER_ID, quarter?.id.orEmpty())
                    putExtra(AppConstants.EXTRA_QUARTER_TITLE, quarter?.title.orEmpty())
                })
            }
        }
    }

    private fun bindHeader(user: User?) {
        binding.tvWelcome.text = "Welcome, ${user?.name ?: "Student"}!"
        val gradeLevel = SchoolStructure.normalizeGradeLevel(user?.gradeLevel.orEmpty())
            .ifBlank { SchoolStructure.defaultGradeLevelForRole("student") }
        val sectionName = SchoolStructure.normalizeSectionName(
            user?.section.orEmpty().ifBlank { user?.sectionId.orEmpty() }
        )
        binding.tvLearningPlanTitle.text = "$gradeLevel Learning Plan"
        binding.tvStudentScope.text = if (sectionName.isBlank()) {
            gradeLevel
        } else {
            "$gradeLevel - $sectionName"
        }
    }

    private fun loadDashboardSnapshot() {
        val user = currentUser ?: return
        val gradeLevel = SchoolStructure.resolveGradeLevel(user.gradeLevel, user.role)
        val section = SchoolStructure.resolveSectionName(user.section.ifBlank { user.sectionId })

        binding.tvLearningPlanSubtitle.text =
            "Follow released lessons, complete teacher tasks, and keep your progress in one place."
        binding.tvNextLessonValue.text = "Checking your next released lesson..."
        binding.tvTaskSnapshot.text = "Checking teacher tasks for your section..."
        binding.tvRecentProgress.text = "Checking your latest progress..."

        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val quarter = quarters.firstOrNull()
            if (quarter == null) {
                runOnUiThread {
                    binding.tvNextLessonValue.text = "No active quarter has been prepared yet."
                    binding.tvTaskSnapshot.text = "Teacher tasks will appear here after curriculum setup."
                    binding.tvRecentProgress.text = "Your progress summary will appear once lessons are released."
                }
                return@getQuartersForGrade
            }

            dbService.getReleasedLessonsForQuarter(quarter.id, section) { lessons ->
                dbService.getStudentLessonProgressMap(user.id) { progressMap ->
                    dbService.getMissionsForQuarter(quarter.id, section, gradeLevel) { missions ->
                        dbService.getStudentMissionProgressMap(user.id) { missionProgressMap ->
                            val orderedLessons = lessons.sortedWith(
                                compareBy<Lesson> { it.orderIndex }.thenBy { it.title }
                            )
                            val nextLesson = orderedLessons.firstOrNull { lesson ->
                                progressMap[lesson.id]?.status != "completed"
                            }
                            val completedLessons = orderedLessons.count { lesson ->
                                progressMap[lesson.id]?.status == "completed"
                            }
                            val activeMissions = missions.filter { mission ->
                                missionProgressMap[mission.id]?.completed != true
                            }.sortedBy { it.title }
                            val completedTasks = missions.count { mission ->
                                missionProgressMap[mission.id]?.completed == true
                            }

                            runOnUiThread {
                                bindStudentSnapshot(
                                    quarterTitle = quarter.title,
                                    lessons = orderedLessons,
                                    nextLesson = nextLesson,
                                    completedLessons = completedLessons,
                                    missions = missions,
                                    activeMissions = activeMissions,
                                    completedTasks = completedTasks,
                                    progressMap = progressMap,
                                    missionProgressMap = missionProgressMap
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindStudentSnapshot(
        quarterTitle: String,
        lessons: List<Lesson>,
        nextLesson: Lesson?,
        completedLessons: Int,
        missions: List<Mission>,
        activeMissions: List<Mission>,
        completedTasks: Int,
        progressMap: Map<String, StudentLessonProgress>,
        missionProgressMap: Map<String, StudentMissionProgress>
    ) {
        val totalLessons = lessons.size
        val nextLessonText = when {
            nextLesson != null -> "Next lesson: ${nextLesson.title}"
            lessons.isNotEmpty() -> "All released lessons for $quarterTitle are completed."
            else -> "No released lessons are available for your section yet."
        }
        val activeTaskText = when {
            activeMissions.isNotEmpty() -> {
                val nextTask = activeMissions.first()
                "${activeMissions.size} active task(s). Start with ${nextTask.title}."
            }
            missions.isNotEmpty() -> "All released teacher tasks are completed."
            else -> "No teacher tasks are released for your section yet."
        }
        val recentLesson = lessons
            .filter { progressMap[it.id]?.status == "completed" }
            .maxByOrNull { progressMap[it.id]?.completedAt.orEmpty() }
        val recentTask = missions
            .filter { missionProgressMap[it.id]?.completed == true }
            .maxByOrNull { missionProgressMap[it.id]?.updatedAt.orEmpty() }
        val recentProgressText = buildString {
            append("$completedLessons of $totalLessons released lesson(s) completed")
            append(" - $completedTasks task(s) done.")
            if (recentLesson != null) {
                append(" Latest lesson: ${recentLesson.title}.")
            } else if (recentTask != null) {
                append(" Latest task: ${recentTask.title}.")
            }
        }

        binding.tvLearningPlanSubtitle.text =
            "Quarter focus: $quarterTitle. Your dashboard reflects what is released to your section."
        binding.tvNextLessonValue.text = nextLessonText
        binding.tvTaskSnapshot.text = activeTaskText
        binding.tvRecentProgress.text = recentProgressText
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
