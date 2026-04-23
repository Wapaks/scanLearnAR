package com.example.scanlearn.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.Quarter
import com.example.scanlearn.databinding.ActivityAddEditMissionBinding
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.SectionRecord
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.repositories.LessonRepository
import com.example.scanlearn.services.AiGovernanceService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.services.TeacherCopilotDefaults
import com.example.scanlearn.services.TeacherCopilotService
import com.example.scanlearn.services.TeacherCopilotServiceFactory
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.SchoolStructure
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class AddEditMissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditMissionBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService
    private val aiService: TeacherCopilotService = TeacherCopilotServiceFactory.create()
    private val aiGovernanceService = AiGovernanceService()
    private val curriculumRepository = CurriculumRepository()
    private val lessonRepository = LessonRepository()

    private var editingMissionId: String = ""
    private var currentMission: Mission? = null
    private var publishedObjects: List<LearningObject> = emptyList()
    private var availableQuarters: List<Quarter> = emptyList()
    private var availableLessons: List<Lesson> = emptyList()
    private var selectedQuarterId: String = ""
    private var selectedLessonId: String = ""
    private var aiTouchedMission = false
    private var sectionCheckBoxes: List<CheckBox> = emptyList()
    private var sectionRecords: List<SectionRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditMissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()
        storageService = StorageService(this)
        editingMissionId = intent.getStringExtra(AppConstants.EXTRA_MISSION_ID) ?: ""

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveMission.setOnClickListener { saveMission() }
        binding.btnGenerateMission.setOnClickListener { generateMissionDraft() }

        loadEditor()
    }

    private fun loadEditor() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        loadSectionOptions {
            renderSectionOptions(emptyList())
            curriculumRepository.getQuartersForGrade(currentTeacherGrade()) { quarters ->
                availableQuarters = quarters.sortedBy { it.orderIndex }
                lessonRepository.getAllLessons { lessons ->
                    availableLessons = lessons
                        .filter { it.gradeLevel.equals(currentTeacherGrade(), ignoreCase = true) }
                        .sortedWith(compareBy<Lesson> { it.quarterId }.thenBy { it.orderIndex }.thenBy { it.title })
                    dbService.getPublishedLearningObjects { objects ->
                        publishedObjects = objects.sortedBy { it.name }
                        runOnUiThread {
                            setupQuarterDropdown()
                            setupLessonDropdown()
                        }

                        if (editingMissionId.isBlank()) {
                            runOnUiThread {
                                binding.progressBar.visibility = android.view.View.GONE
                                binding.tvScreenTitle.text = "Create Mission"
                                renderObjectOptions(emptyList())
                                preselectDefaultQuarter()
                            }
                        } else {
                            dbService.getMission(editingMissionId) { mission ->
                                currentMission = mission
                                runOnUiThread {
                                    binding.progressBar.visibility = android.view.View.GONE
                                    if (mission == null) {
                                        Toast.makeText(this, "Could not load mission.", Toast.LENGTH_SHORT).show()
                                        finish()
                                        return@runOnUiThread
                                    }
                                    binding.tvScreenTitle.text = "Edit Mission"
                                    bindMission(mission)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindMission(mission: Mission) {
        binding.etMissionTitle.setText(mission.title)
        binding.etMissionDescription.setText(mission.description)
        binding.etCategory.setText(mission.category)
        renderSectionOptions(mission.sectionIds)
        binding.switchActive.isChecked = mission.active
        selectedQuarterId = mission.quarterId
        selectedLessonId = mission.lessonIds.firstOrNull().orEmpty()
        availableQuarters.firstOrNull { it.id == selectedQuarterId }?.let {
            binding.etQuarter.setText(it.title, false)
        }
        syncLessonOptionsForQuarter(selectedQuarterId)
        availableLessons.firstOrNull { it.id == selectedLessonId }?.let {
            binding.etLesson.setText(it.title, false)
        }
        renderObjectOptions(mission.objectsToFind)
    }

    private fun renderObjectOptions(selectedIds: List<String>) {
        binding.objectsContainer.removeAllViews()

        publishedObjects.forEach { obj ->
            val checkBox = CheckBox(this)
            checkBox.text = "${obj.name} (${obj.category})"
            checkBox.tag = obj.id
            checkBox.isChecked = selectedIds.contains(obj.id)
            binding.objectsContainer.addView(checkBox)
        }
    }

    private fun renderSectionOptions(selectedSections: List<String>) {
        binding.sectionsContainer.removeAllViews()
        val teacherSections = sectionRecords
            .filter { it.gradeLevel.equals(currentTeacherGrade(), ignoreCase = true) }
            .map { it.name }
            .ifEmpty { SchoolStructure.sectionsForGrade(currentTeacherGrade()) }
        sectionCheckBoxes = teacherSections.map { sectionName ->
            CheckBox(this).apply {
                text = sectionName
                isChecked = selectedSections.any { it.equals(sectionName, ignoreCase = true) }
            }.also { binding.sectionsContainer.addView(it) }
        }
    }

    private fun setupQuarterDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            availableQuarters.map { it.title }
        )
        binding.etQuarter.setAdapter(adapter)
        binding.etQuarter.setOnItemClickListener { _, _, position, _ ->
            val quarter = availableQuarters.getOrNull(position) ?: return@setOnItemClickListener
            selectedQuarterId = quarter.id
            binding.tilQuarter.error = null
            selectedLessonId = ""
            binding.etLesson.setText("", false)
            syncLessonOptionsForQuarter(selectedQuarterId)
        }
    }

    private fun setupLessonDropdown() {
        syncLessonOptionsForQuarter(selectedQuarterId)
        binding.etLesson.setOnItemClickListener { _, _, position, _ ->
            val lessonsForQuarter = getLessonsForSelectedQuarter()
            val lesson = lessonsForQuarter.getOrNull(position) ?: return@setOnItemClickListener
            selectedLessonId = lesson.id
            binding.tilLesson.error = null
            if (binding.etCategory.text.isNullOrBlank()) {
                binding.etCategory.setText(lesson.lessonType.ifBlank { "science" })
            }
            if (currentMission == null) {
                renderObjectOptions(lesson.linkedObjectIds)
            }
        }
    }

    private fun preselectDefaultQuarter() {
        val quarter = availableQuarters.firstOrNull() ?: return
        selectedQuarterId = quarter.id
        binding.etQuarter.setText(quarter.title, false)
        syncLessonOptionsForQuarter(selectedQuarterId)
    }

    private fun syncLessonOptionsForQuarter(quarterId: String) {
        val lessonsForQuarter = getLessonsForSelectedQuarter(quarterId)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            lessonsForQuarter.map { it.title }
        )
        binding.etLesson.setAdapter(adapter)
    }

    private fun getLessonsForSelectedQuarter(quarterId: String = selectedQuarterId): List<Lesson> {
        return availableLessons.filter { quarterId.isBlank() || it.quarterId == quarterId }
    }

    private fun saveMission() {
        val title = binding.etMissionTitle.text.toString().trim()
        val description = binding.etMissionDescription.text.toString().trim()
        val category = binding.etCategory.text.toString().trim().lowercase()
        val selectedQuarter = availableQuarters.firstOrNull {
            it.id == selectedQuarterId || it.title == binding.etQuarter.text.toString().trim()
        }
        val selectedLesson = getLessonsForSelectedQuarter(selectedQuarter?.id.orEmpty()).firstOrNull {
            it.id == selectedLessonId || it.title == binding.etLesson.text.toString().trim()
        }
        val sectionIds = buildList {
            sectionCheckBoxes.filter { it.isChecked }.forEach { add(it.text.toString()) }
        }
        val selectedObjectIds = (0 until binding.objectsContainer.childCount)
            .mapNotNull { index -> binding.objectsContainer.getChildAt(index) as? CheckBox }
            .filter { it.isChecked }
            .mapNotNull { it.tag as? String }

        if (title.isBlank()) {
            binding.tilMissionTitle.error = "Title is required"
            return
        }
        if (description.isBlank()) {
            binding.tilMissionDescription.error = "Description is required"
            return
        }
        if (category.isBlank()) {
            binding.tilCategory.error = "Category is required"
            return
        }
        if (selectedQuarter == null) {
            binding.tilQuarter.error = "Quarter is required"
            return
        }
        if (selectedLesson == null) {
            binding.tilLesson.error = "Lesson link is required"
            return
        }
        if (sectionIds.isEmpty()) {
            Toast.makeText(this, "Select at least one section.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedObjectIds.isEmpty()) {
            Toast.makeText(this, "Select at least one object for the mission.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tilMissionTitle.error = null
        binding.tilMissionDescription.error = null
        binding.tilCategory.error = null
        binding.tilQuarter.error = null
        binding.tilLesson.error = null
        binding.btnSaveMission.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE

        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        val currentUserId = storageService.getUser()?.id ?: ""
        val existing = currentMission
        val lessonReleaseMismatch = selectedLesson.releasedSectionIds.isNotEmpty() &&
            sectionIds.any { section -> selectedLesson.releasedSectionIds.none { it.equals(section, ignoreCase = true) } }

        if (binding.switchActive.isChecked && !selectedLesson.status.equals("published", ignoreCase = true)) {
            binding.progressBar.visibility = android.view.View.GONE
            binding.btnSaveMission.isEnabled = true
            Toast.makeText(
                this,
                "Publish the linked lesson before releasing this mission to students.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (binding.switchActive.isChecked && lessonReleaseMismatch) {
            binding.progressBar.visibility = android.view.View.GONE
            binding.btnSaveMission.isEnabled = true
            Toast.makeText(
                this,
                "This mission can only be active in sections where the linked lesson is already released.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val mission = Mission(
            id = existing?.id ?: "mission_${System.currentTimeMillis()}",
            title = title,
            description = description,
            missionType = existing?.missionType?.ifBlank { "curriculum_scan" } ?: "curriculum_scan",
            gradeLevel = selectedQuarter.gradeLevel.ifBlank { currentTeacherGrade() },
            quarterId = selectedQuarter.id,
            lessonIds = listOf(selectedLesson.id),
            objectsToFind = selectedObjectIds,
            sectionIds = sectionIds,
            releasedSectionIds = sectionIds,
            category = category,
            active = binding.switchActive.isChecked,
            createdBy = existing?.createdBy?.ifBlank { currentUserId } ?: currentUserId,
            createdAt = existing?.createdAt?.ifBlank { now } ?: now,
            updatedAt = now,
            aiGenerated = existing?.aiGenerated == true || aiTouchedMission,
            aiSource = if (existing?.aiGenerated == true || aiTouchedMission) TeacherCopilotDefaults.DEFAULT_MODEL else existing?.aiSource.orEmpty(),
            aiPromptVersion = if (existing?.aiGenerated == true || aiTouchedMission) TeacherCopilotDefaults.PROMPT_VERSION else existing?.aiPromptVersion.orEmpty(),
            recommendedForStudentId = existing?.recommendedForStudentId ?: ""
        )

        dbService.saveMission(mission) { success ->
            runOnUiThread {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnSaveMission.isEnabled = true
                if (success) {
                    aiTouchedMission = false
                    Toast.makeText(this, "Mission saved successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Could not save mission.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun generateMissionDraft() {
        val currentUser = storageService.getUser()
        val selectedObjectNames = (0 until binding.objectsContainer.childCount)
            .mapNotNull { index -> binding.objectsContainer.getChildAt(index) as? CheckBox }
            .filter { it.isChecked }
            .map { it.text.toString().substringBefore(" (") }

        val sectionIds = buildList {
            sectionCheckBoxes.filter { it.isChecked }.forEach { add(it.text.toString()) }
        }

        binding.tvAiMissionStatus.text = "Generating mission draft..."
        lifecycleScope.launch {
            try {
                val suggestion = aiService.generateMissionDraft(
                    gradeLevel = currentTeacherGrade(),
                    sectionNames = sectionIds.ifEmpty { SchoolStructure.sectionsForGrade(currentTeacherGrade()).take(1) },
                    category = binding.etCategory.text?.toString().orEmpty().ifBlank { "science" },
                    objectNames = selectedObjectNames.ifEmpty { publishedObjects.take(3).map { it.name } }
                )
                binding.etMissionTitle.setText(suggestion.title.ifBlank { binding.etMissionTitle.text })
                binding.etMissionDescription.setText(
                    suggestion.description.ifBlank { binding.etMissionDescription.text }
                )
                if (binding.etCategory.text.isNullOrBlank()) {
                    binding.etCategory.setText(
                        suggestion.category.ifBlank {
                            availableLessons.firstOrNull { it.id == selectedLessonId }?.lessonType ?: "science"
                        }
                    )
                }
                aiTouchedMission = true
                aiGovernanceService.saveDraftVariant(
                    targetType = "mission",
                    targetId = currentMission?.id ?: editingMissionId.ifBlank { "new_mission" },
                    feature = "mission_generation",
                    generatedText = suggestion.rawText,
                    createdBy = currentUser?.id.orEmpty(),
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION
                )
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "mission_generation",
                    targetType = "mission",
                    targetId = currentMission?.id ?: editingMissionId.ifBlank { "new_mission" },
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION,
                    status = "success"
                )
                binding.tvAiMissionStatus.text = "Gemini drafted a mission. Review it before saving."
            } catch (e: Exception) {
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "mission_generation",
                    targetType = "mission",
                    targetId = currentMission?.id ?: editingMissionId.ifBlank { "new_mission" },
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION,
                    status = "failed",
                    errorMessage = e.message.orEmpty()
                )
                binding.tvAiMissionStatus.text = "Gemini mission draft failed: ${e.message ?: "unknown error"}"
            }
        }
    }

    private fun currentTeacherGrade(): String {
        return SchoolStructure.normalizeGradeLevel(storageService.getUser()?.gradeLevel.orEmpty())
            .ifBlank { SchoolStructure.defaultGradeLevelForRole("teacher") }
    }

    private fun loadSectionOptions(onComplete: () -> Unit) {
        dbService.getSectionsForGrade(currentTeacherGrade()) { sections ->
            sectionRecords = sections
            runOnUiThread { onComplete() }
        }
    }
}
