package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityTeacherBinding
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.SectionRecord
import com.example.scanlearn.models.StudentLessonProgress
import com.example.scanlearn.models.StudentMissionProgress
import com.example.scanlearn.models.User
import com.example.scanlearn.services.FirebaseAuthService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.SchoolStructure
import com.google.firebase.database.ValueEventListener

class TeacherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherBinding
    private lateinit var storage: StorageService
    private lateinit var authService: FirebaseAuthService
    private lateinit var dbService: RealtimeDbService
    private var currentUser: User? = null
    private var conversationsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        authService = FirebaseAuthService()
        dbService = RealtimeDbService()

        currentUser = storage.getUser()
        bindHeader()
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
        binding.btnReviewQueue.setOnClickListener {
            startActivity(Intent(this, TeacherReviewQueueActivity::class.java))
        }
        binding.btnStudents.setOnClickListener {
            startActivity(Intent(this, TeacherStudentsActivity::class.java))
        }
        binding.btnSections.setOnClickListener {
            startActivity(Intent(this, TeacherSectionsActivity::class.java))
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
        currentUser = storage.getUser()
        bindHeader()
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

    private fun bindHeader() {
        val teacher = currentUser
        val teacherGrade = teacherGrade()
        binding.tvTeacherName.text = teacher?.name ?: "Teacher"
        binding.tvTeacherEmail.text = teacher?.email ?: ""
        binding.tvTeacherScope.text = "Handles $teacherGrade"
        binding.tvDashboardSummary.text =
            "Track released lessons, teacher tasks, and completion signals for $teacherGrade."
    }

    private fun loadOverview() {
        setLoadingState(true)
        val teacherGrade = teacherGrade()

        dbService.getSectionsForGrade(teacherGrade) { sections ->
            dbService.getAllStudents { students ->
                dbService.getAllLessons { lessons ->
                    dbService.getAllMissions { missions ->
                        dbService.getAllStudentLessonProgressMaps { lessonProgressMaps ->
                            dbService.getAllStudentMissionProgressMaps { missionProgressMaps ->
                                val studentsInScope = students.filter {
                                    it.gradeLevel.equals(teacherGrade, ignoreCase = true)
                                }
                                val sectionNames = sections.map { it.name }.ifEmpty {
                                    SchoolStructure.sectionsForGrade(teacherGrade)
                                }
                                val lessonsInScope = lessons.filter { lesson ->
                                    lesson.gradeLevel.equals(teacherGrade, ignoreCase = true) &&
                                        lesson.status.equals("published", ignoreCase = true)
                                }
                                val releasedLessons = lessonsInScope.filter { it.releasedSectionIds.isNotEmpty() }
                                val missionsInScope = missions.filter { mission ->
                                    mission.gradeLevel.equals(teacherGrade, ignoreCase = true) && mission.active
                                }
                                val releasedMissions = missionsInScope.filter { it.releasedSectionIds.isNotEmpty() }
                                val studentIdsInScope = studentsInScope.map { it.id }.toSet()
                                val scopedLessonProgressMaps = lessonProgressMaps.filterKeys { it in studentIdsInScope }
                                val scopedMissionProgressMaps = missionProgressMaps.filterKeys { it in studentIdsInScope }
                                val totalCompletedLessons = scopedLessonProgressMaps.values.sumOf { progressMap ->
                                    progressMap.values.count { it.status.equals("completed", ignoreCase = true) }
                                }
                                val totalCompletedTasks = scopedMissionProgressMaps.values.sumOf { progressMap ->
                                    progressMap.values.count { it.completed }
                                }
                                val sectionSummaries = sectionNames.map { sectionName ->
                                    buildSectionSummary(
                                        sectionName = sectionName,
                                        students = studentsInScope,
                                        releasedLessons = releasedLessons,
                                        releasedMissions = releasedMissions,
                                        lessonProgressMaps = scopedLessonProgressMaps,
                                        missionProgressMaps = scopedMissionProgressMaps
                                    )
                                }
                                val spotlightSection = sectionSummaries.maxWithOrNull(
                                    compareBy<SectionSnapshot> { it.lessonRunsCompleted + it.taskRunsCompleted }
                                        .thenBy { it.activeTasks }
                                        .thenBy { it.learners }
                                )

                                runOnUiThread {
                                    setLoadingState(false)
                                    bindOverview(
                                        teacherGrade = teacherGrade,
                                        studentsInScope = studentsInScope,
                                        releasedLessons = releasedLessons,
                                        releasedMissions = releasedMissions,
                                        totalCompletedLessons = totalCompletedLessons,
                                        totalCompletedTasks = totalCompletedTasks,
                                        spotlightSection = spotlightSection
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindOverview(
        teacherGrade: String,
        studentsInScope: List<User>,
        releasedLessons: List<Lesson>,
        releasedMissions: List<Mission>,
        totalCompletedLessons: Int,
        totalCompletedTasks: Int,
        spotlightSection: SectionSnapshot?
    ) {
        binding.tvDashboardSummary.text =
            "$teacherGrade now shows the same release and completion truth your learners see in the LMS."
        binding.tvStudentsSummary.text = "${studentsInScope.size} learners in $teacherGrade"
        binding.tvStudentsSummaryHint.text =
            "$totalCompletedLessons lesson completions and $totalCompletedTasks task completions recorded so far."

        binding.tvObjectsSummary.text = "${releasedLessons.size} released lessons"
        binding.tvObjectsSummaryHint.text = if (releasedLessons.isEmpty()) {
            "No published lessons are released to sections yet."
        } else {
            val latestLesson = releasedLessons.maxByOrNull { it.updatedAt }
            val releasedTo = latestLesson?.releasedSectionIds?.joinToString(", ").orEmpty()
            "Latest release: ${latestLesson?.title ?: "Lesson"}${if (releasedTo.isBlank()) "" else " -> $releasedTo"}."
        }

        binding.tvMissionsSummary.text = "${releasedMissions.size} active teacher tasks"
        binding.tvMissionsSummaryHint.text = if (releasedMissions.isEmpty()) {
            "No active tasks are released to sections yet."
        } else {
            val taskNames = releasedMissions.take(2).joinToString(", ") { it.title }
            "Visible in section dashboards now: $taskNames${if (releasedMissions.size > 2) "..." else ""}"
        }

        binding.tvSectionReleaseSummary.text = if (spotlightSection == null) {
            "Section spotlight will appear once sections have learners or releases."
        } else {
            "${spotlightSection.sectionName}: ${spotlightSection.releasedLessons} lesson release(s), ${spotlightSection.activeTasks} active task(s), ${spotlightSection.learners} learner(s)."
        }
        binding.tvSectionCompletionSummary.text = if (spotlightSection == null) {
            "Completion trends will appear here after the first release cycle."
        } else {
            "${spotlightSection.sectionName} has ${spotlightSection.lessonRunsCompleted} lesson completion(s) and ${spotlightSection.taskRunsCompleted} task completion(s)."
        }
    }

    private fun buildSectionSummary(
        sectionName: String,
        students: List<User>,
        releasedLessons: List<Lesson>,
        releasedMissions: List<Mission>,
        lessonProgressMaps: Map<String, Map<String, StudentLessonProgress>>,
        missionProgressMaps: Map<String, Map<String, StudentMissionProgress>>
    ): SectionSnapshot {
        val sectionStudents = students.filter {
            SchoolStructure.resolveSectionName(it.section.ifBlank { it.sectionId })
                .equals(sectionName, ignoreCase = true)
        }
        val sectionStudentIds = sectionStudents.map { it.id }.toSet()
        val sectionReleasedLessons = releasedLessons.count { lesson ->
            lesson.releasedSectionIds.any { it.equals(sectionName, ignoreCase = true) }
        }
        val sectionActiveTasks = releasedMissions.count { mission ->
            mission.releasedSectionIds.any { it.equals(sectionName, ignoreCase = true) }
        }
        val lessonRunsCompleted = lessonProgressMaps
            .filterKeys { it in sectionStudentIds }
            .values
            .sumOf { progressMap -> progressMap.values.count { it.status.equals("completed", ignoreCase = true) } }
        val taskRunsCompleted = missionProgressMaps
            .filterKeys { it in sectionStudentIds }
            .values
            .sumOf { progressMap -> progressMap.values.count { it.completed } }

        return SectionSnapshot(
            sectionName = sectionName,
            learners = sectionStudents.size,
            releasedLessons = sectionReleasedLessons,
            activeTasks = sectionActiveTasks,
            lessonRunsCompleted = lessonRunsCompleted,
            taskRunsCompleted = taskRunsCompleted
        )
    }

    private fun teacherGrade(): String {
        return SchoolStructure.normalizeGradeLevel(currentUser?.gradeLevel.orEmpty())
            .ifBlank { SchoolStructure.defaultGradeLevelForRole("teacher") }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.overviewContent.visibility = if (isLoading) View.GONE else View.VISIBLE
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

    private data class SectionSnapshot(
        val sectionName: String,
        val learners: Int,
        val releasedLessons: Int,
        val activeTasks: Int,
        val lessonRunsCompleted: Int,
        val taskRunsCompleted: Int
    )
}
