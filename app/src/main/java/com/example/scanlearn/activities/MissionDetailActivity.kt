package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityMissionDetailBinding
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.StudentMissionProgress
import com.example.scanlearn.repositories.LessonRepository
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MissionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMissionDetailBinding
    private lateinit var storage: StorageService
    private lateinit var dbService: RealtimeDbService
    private val lessonRepository = LessonRepository()

    private var missionId: String = ""
    private var currentMission: Mission? = null
    private var currentProgress: StudentMissionProgress? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        dbService = RealtimeDbService()
        missionId = intent.getStringExtra(AppConstants.EXTRA_MISSION_ID).orEmpty()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnOpenLesson.setOnClickListener { openLinkedLesson() }
        binding.btnUseScanner.setOnClickListener { openScannerMode() }
        binding.btnMarkComplete.setOnClickListener { markTaskComplete() }

        loadMission()
    }

    override fun onResume() {
        super.onResume()
        if (missionId.isNotBlank()) {
            loadMission()
        }
    }

    private fun loadMission() {
        val user = storage.getUser()
        if (missionId.isBlank() || user == null) {
            finish()
            return
        }

        binding.loadingIndicator.visibility = View.VISIBLE
        binding.contentGroup.visibility = View.GONE

        dbService.getMission(missionId) { mission ->
            if (mission == null) {
                runOnUiThread {
                    Toast.makeText(this, "Could not load this teacher task.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@getMission
            }

            dbService.getStudentMissionProgressMap(user.id) { progressMap ->
                currentMission = mission
                currentProgress = progressMap[mission.id]
                runOnUiThread {
                    binding.loadingIndicator.visibility = View.GONE
                    binding.contentGroup.visibility = View.VISIBLE
                    bindMission(mission, progressMap[mission.id])
                }
            }
        }
    }

    private fun bindMission(mission: Mission, progress: StudentMissionProgress?) {
        val completed = progress?.completed == true
        val linkedLessons = mission.lessonIds.size
        val taskFocus = if (mission.objectsToFind.isEmpty()) {
            "Follow the teacher instructions and finish the linked lesson activity."
        } else {
            mission.objectsToFind.joinToString(", ")
        }

        binding.tvTaskTitle.text = mission.title
        binding.tvTaskDescription.text = mission.description
        binding.tvTaskMeta.text =
            "${mission.gradeLevel.ifBlank { "Your grade" }} - ${mission.progressPercentForDisplay(progress)}% complete"
        binding.tvTaskFocus.text = "Task focus: $taskFocus"
        binding.tvTaskLinks.text = if (linkedLessons > 0) {
            "This task is linked to $linkedLessons lesson(s). Open the lesson first, then come back and complete this task."
        } else {
            "This task can be completed directly from the LMS."
        }
        binding.tvTaskStatus.text = if (completed) {
            "Completed. Your teacher can now see this in the section view."
        } else {
            "Active. Complete this task when you finish the linked work."
        }

        binding.btnOpenLesson.isEnabled = mission.lessonIds.isNotEmpty()
        binding.btnUseScanner.visibility = if (mission.objectsToFind.isNotEmpty()) View.VISIBLE else View.GONE
        binding.btnMarkComplete.isEnabled = !completed
        binding.btnMarkComplete.text = if (completed) "Task Completed" else "Mark Task Complete"
    }

    private fun openLinkedLesson() {
        val lessonId = currentMission?.lessonIds?.firstOrNull()
        if (lessonId.isNullOrBlank()) {
            Toast.makeText(this, "No linked lesson is available for this task.", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, LessonPlayerActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_LESSON_ID, lessonId)
        })
    }

    private fun openScannerMode() {
        val mission = currentMission ?: return
        startActivity(Intent(this, ScannerActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_MODE, AppConstants.MODE_MISSION)
            putExtra(AppConstants.EXTRA_MISSION_ID, mission.id)
            putExtra(AppConstants.EXTRA_MISSION_TITLE, mission.title)
        })
    }

    private fun markTaskComplete() {
        val user = storage.getUser()
        val mission = currentMission
        if (user == null || mission == null) return

        binding.btnMarkComplete.isEnabled = false
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        val completedObjects = mission.objectsToFind.distinct()
        val updatedProgress = StudentMissionProgress(
            missionId = mission.id,
            completedObjectIds = completedObjects,
            progressPercent = 100,
            completed = true,
            updatedAt = timestamp
        )

        dbService.saveStudentMissionProgress(user.id, updatedProgress) { success ->
            runOnUiThread {
                if (!success) {
                    binding.btnMarkComplete.isEnabled = true
                    Toast.makeText(this, "Could not save task completion.", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                currentProgress = updatedProgress
                bindMission(mission, updatedProgress)
                Toast.makeText(
                    this,
                    "Task completed. Your teacher can now see this in the section view.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun Mission.progressPercentForDisplay(progress: StudentMissionProgress?): Int {
        return progress?.progressPercent ?: progressPercent
    }
}
