package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.MissionAdapter
import com.example.scanlearn.databinding.ActivityMissionsBinding
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.Mission
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.repositories.LessonRepository
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.SchoolStructure

class MissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMissionsBinding
    private lateinit var storage: StorageService
    private lateinit var dbService: RealtimeDbService
    private val curriculumRepository = CurriculumRepository()
    private val lessonRepository = LessonRepository()
    private var missions: List<Mission> = emptyList()
    private var quarterId: String = ""
    private var quarterTitle: String = ""
    private var lessonId: String = ""
    private var lessonTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        dbService = RealtimeDbService()
        quarterId = intent.getStringExtra(AppConstants.EXTRA_QUARTER_ID).orEmpty()
        quarterTitle = intent.getStringExtra(AppConstants.EXTRA_QUARTER_TITLE).orEmpty()
        lessonId = intent.getStringExtra(AppConstants.EXTRA_LESSON_ID).orEmpty()
        lessonTitle = intent.getStringExtra(AppConstants.EXTRA_LESSON_TITLE).orEmpty()

        binding.btnBack.setOnClickListener { finish() }

        loadMissions()
    }

    private fun loadMissions() {
        val user = storage.getUser()
        if (user == null) {
            finish()
            return
        }
        val section = SchoolStructure.resolveSectionName(user.section.ifBlank { user.sectionId })
        val gradeLevel = SchoolStructure.resolveGradeLevel(user.gradeLevel, user.role)

        if (lessonId.isNotBlank()) {
            binding.tvHeaderTitle.text = lessonTitle.ifBlank { "Lesson Missions" }
            binding.tvHeaderSubtitle.text = "Mission work aligned to this lesson."
        } else if (quarterId.isNotBlank()) {
            binding.tvHeaderTitle.text = quarterTitle.ifBlank { "Quarter Missions" }
            binding.tvHeaderSubtitle.text = "Mission work connected to the current quarter."
        }

        collectVisibleLessons(user.id, gradeLevel, section) { visibleLessons ->
            val visibleLessonIds = visibleLessons.map { it.id }.toSet()
            val missionLoader: (((List<Mission>) -> Unit) -> Unit) = { callback ->
                when {
                    lessonId.isNotBlank() -> dbService.getMissionsForLesson(
                        lessonId = lessonId,
                        section = section,
                        gradeLevel = gradeLevel,
                        quarterId = quarterId,
                        onResult = callback
                    )
                    quarterId.isNotBlank() -> dbService.getMissionsForQuarter(
                        quarterId = quarterId,
                        section = section,
                        gradeLevel = gradeLevel,
                        onResult = callback
                    )
                    else -> dbService.getMissionsForStudent(
                        section = section,
                        gradeLevel = gradeLevel,
                        onResult = callback
                    )
                }
            }

            missionLoader { firebaseMissions ->
                dbService.getStudentMissionProgressMap(user.id) { progressMap ->
                    runOnUiThread {
                        missions = firebaseMissions
                            .filter { mission ->
                                mission.lessonIds.isEmpty() || mission.lessonIds.any { it in visibleLessonIds }
                            }
                            .map { mission ->
                                val progress = progressMap[mission.id]
                                mission.copy(
                                    completed = progress?.completed ?: false,
                                    progressPercent = progress?.progressPercent ?: 0,
                                    completedObjectIds = progress?.completedObjectIds ?: emptyList()
                                )
                            }

                        binding.tvHeaderSubtitle.text = if (missions.isEmpty()) {
                            "No released tasks yet. Your teacher can release lesson-linked work to your section."
                        } else {
                            "${missions.count { !it.completed }} active task(s) are ready for your section."
                        }
                        binding.tvEmptyState.visibility = if (missions.isEmpty()) View.VISIBLE else View.GONE
                        binding.rvMissions.visibility = if (missions.isEmpty()) View.GONE else View.VISIBLE
                        binding.rvMissions.layoutManager = LinearLayoutManager(this)
                        binding.rvMissions.adapter = MissionAdapter(missions) { mission ->
                            val intent = Intent(this, MissionDetailActivity::class.java)
                            intent.putExtra(AppConstants.EXTRA_MISSION_ID, mission.id)
                            intent.putExtra(AppConstants.EXTRA_MISSION_TITLE, mission.title)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    private fun collectVisibleLessons(
        userId: String,
        gradeLevel: String,
        section: String,
        onResult: (List<Lesson>) -> Unit
    ) {
        if (quarterId.isNotBlank()) {
            lessonRepository.getReleasedLessonsForQuarter(quarterId, section, onResult)
            return
        }

        curriculumRepository.getQuartersForGrade(gradeLevel) { quarters ->
            val activeQuarter = quarters.firstOrNull()
            if (activeQuarter == null) {
                onResult(emptyList())
                return@getQuartersForGrade
            }
            lessonRepository.getReleasedLessonsForQuarter(activeQuarter.id, section, onResult)
        }
    }
}
