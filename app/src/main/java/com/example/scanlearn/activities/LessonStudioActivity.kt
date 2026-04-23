package com.example.scanlearn.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scanlearn.databinding.ActivityLessonStudioBinding
import com.example.scanlearn.databinding.ItemLessonActivityEditorBinding
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.LessonActivity
import com.example.scanlearn.models.SectionRecord
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

class LessonStudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonStudioBinding
    private val lessonRepository = LessonRepository()
    private val aiService: TeacherCopilotService = TeacherCopilotServiceFactory.create()
    private val aiGovernanceService = AiGovernanceService()
    private val dbService = RealtimeDbService()
    private lateinit var storageService: StorageService
    private lateinit var lesson: Lesson
    private val activityBindings = mutableListOf<ItemLessonActivityEditorBinding>()
    private var aiTouchedLesson = false
    private val releaseCheckBoxes = mutableListOf<android.widget.CheckBox>()
    private var sectionRecords: List<SectionRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLessonStudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storageService = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddActivity.setOnClickListener { addActivityRow() }
        binding.btnGenerateDraft.setOnClickListener { generateLessonDraft() }
        binding.btnSimplifyLesson.setOnClickListener { simplifyLesson() }
        binding.btnGenerateQuiz.setOnClickListener { generateQuizActivities() }
        binding.btnSaveDraft.setOnClickListener { saveLesson("draft") }
        binding.btnSendReview.setOnClickListener { saveLesson("review") }
        binding.btnPublish.setOnClickListener { saveLesson("published") }

        loadLesson()
    }

    private fun loadLesson() {
        val lessonId = intent.getStringExtra(AppConstants.EXTRA_LESSON_ID).orEmpty()
        if (lessonId.isBlank()) {
            finish()
            return
        }

        lessonRepository.getLesson(lessonId) { loadedLesson ->
            if (loadedLesson == null) {
                runOnUiThread {
                    Toast.makeText(this, "Could not load this lesson.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@getLesson
            }
            lesson = loadedLesson
            dbService.getSectionsForGrade(lesson.gradeLevel) { sections ->
                sectionRecords = sections
                lessonRepository.getActivitiesForLesson(lesson.id) { activities ->
                    runOnUiThread {
                        bindLesson(activities)
                    }
                }
            }
        }
    }

    private fun bindLesson(activities: List<LessonActivity>) {
        binding.tvLessonLabel.text = lesson.title
        binding.etTitle.setText(lesson.title)
        binding.etObjective.setText(lesson.objective)
        binding.etSummary.setText(lesson.summary)
        binding.tvStatus.text = "Status: ${lesson.status.replaceFirstChar { it.uppercase() }}"
        renderReleaseSectionOptions(lesson.releasedSectionIds)
        binding.activityContainer.removeAllViews()
        activityBindings.clear()
        binding.tvAiStatus.text = "Use Gemini to draft, simplify, or generate activities, then review before publishing."

        if (activities.isEmpty()) {
            addActivityRow()
        } else {
            activities.sortedBy { it.orderIndex }.forEach { addActivityRow(it) }
        }
    }

    private fun renderReleaseSectionOptions(selectedSections: List<String>) {
        binding.releaseSectionsContainer.removeAllViews()
        releaseCheckBoxes.clear()
        sectionRecords.map { it.name }
            .ifEmpty { SchoolStructure.sectionsForGrade(lesson.gradeLevel) }
            .forEach { sectionName ->
            val checkBox = android.widget.CheckBox(this).apply {
                text = sectionName
                isChecked = selectedSections.any { it.equals(sectionName, ignoreCase = true) }
                setOnCheckedChangeListener { _, _ -> updateReleaseHint() }
            }
            binding.releaseSectionsContainer.addView(checkBox)
            releaseCheckBoxes.add(checkBox)
        }
        updateReleaseHint()
    }

    private fun addActivityRow(activity: LessonActivity? = null) {
        val row = ItemLessonActivityEditorBinding.inflate(LayoutInflater.from(this), binding.activityContainer, false)
        row.etPrompt.setText(activity?.prompt.orEmpty())
        row.etInstructions.setText(activity?.instructions.orEmpty())
        row.etOption1.setText(activity?.options?.getOrNull(0).orEmpty())
        row.etOption2.setText(activity?.options?.getOrNull(1).orEmpty())
        row.etOption3.setText(activity?.options?.getOrNull(2).orEmpty())
        row.etOption4.setText(activity?.options?.getOrNull(3).orEmpty())
        row.etAnswer.setText(activity?.correctAnswer.orEmpty())

        val type = activity?.type ?: "multiple_choice"
        row.rbMultipleChoice.isChecked = type.equals("multiple_choice", ignoreCase = true)
        row.rbShortAnswer.isChecked = type.equals("short_answer", ignoreCase = true)
        toggleOptionFields(row, row.rbMultipleChoice.isChecked)
        row.rgType.setOnCheckedChangeListener { _, checkedId ->
            toggleOptionFields(row, checkedId == row.rbMultipleChoice.id)
        }
        row.btnRemove.setOnClickListener {
            binding.activityContainer.removeView(row.root)
            activityBindings.remove(row)
        }

        binding.activityContainer.addView(row.root)
        activityBindings.add(row)
    }

    private fun toggleOptionFields(
        row: ItemLessonActivityEditorBinding,
        showOptions: Boolean
    ) {
        val visibility = if (showOptions) android.view.View.VISIBLE else android.view.View.GONE
        row.optionsGroup.visibility = visibility
    }

    private fun saveLesson(status: String) {
        val updatedTitle = binding.etTitle.text.toString().trim()
        val updatedObjective = binding.etObjective.text.toString().trim()
        val updatedSummary = binding.etSummary.text.toString().trim()
        val releasedSectionIds = releaseCheckBoxes
            .filter { it.isChecked }
            .map { it.text.toString() }

        if (updatedTitle.isBlank() || updatedObjective.isBlank() || updatedSummary.isBlank()) {
            Toast.makeText(this, "Fill in the lesson title, objective, and summary.", Toast.LENGTH_SHORT).show()
            return
        }
        if (status == "published" && releasedSectionIds.isEmpty()) {
            Toast.makeText(this, "Choose at least one section before publishing.", Toast.LENGTH_SHORT).show()
            return
        }

        val activities = mutableListOf<LessonActivity>()
        activityBindings.forEachIndexed { index, row ->
            val prompt = row.etPrompt.text.toString().trim()
            val instructions = row.etInstructions.text.toString().trim()
            val answer = row.etAnswer.text.toString().trim()
            val isMultipleChoice = row.rbMultipleChoice.isChecked

            if (prompt.isBlank() || answer.isBlank()) {
                return@forEachIndexed
            }

            val options = if (isMultipleChoice) {
                listOf(
                    row.etOption1.text.toString().trim(),
                    row.etOption2.text.toString().trim(),
                    row.etOption3.text.toString().trim(),
                    row.etOption4.text.toString().trim()
                ).filter { it.isNotBlank() }
            } else {
                emptyList()
            }

            if (isMultipleChoice && options.size < 2) {
                return@forEachIndexed
            }

            activities.add(
                LessonActivity(
                    id = "${lesson.id}_activity_${index + 1}",
                    lessonId = lesson.id,
                    type = if (isMultipleChoice) "multiple_choice" else "short_answer",
                    prompt = prompt,
                    instructions = instructions,
                    content = instructions,
                    options = options,
                    correctAnswer = answer,
                    explanation = answer,
                    points = 1,
                    orderIndex = index + 1
                )
            )
        }

        if (activities.isEmpty()) {
            Toast.makeText(this, "Add at least one valid activity before saving.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedLesson = lesson.copy(
            title = updatedTitle,
            objective = updatedObjective,
            summary = updatedSummary,
            activityIds = activities.map { it.id },
            status = status,
            releasedSectionIds = releasedSectionIds,
            aiGenerated = lesson.aiGenerated || aiTouchedLesson,
            aiSource = if (lesson.aiGenerated || aiTouchedLesson) TeacherCopilotDefaults.DEFAULT_MODEL else lesson.aiSource,
            aiPromptVersion = if (lesson.aiGenerated || aiTouchedLesson) TeacherCopilotDefaults.PROMPT_VERSION else lesson.aiPromptVersion,
            updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        )

        binding.btnSaveDraft.isEnabled = false
        binding.btnSendReview.isEnabled = false
        binding.btnPublish.isEnabled = false

        lessonRepository.saveLesson(updatedLesson) { lessonSaved ->
            if (!lessonSaved) {
                runOnUiThread {
                    restoreButtons()
                    Toast.makeText(this, "Could not save the lesson.", Toast.LENGTH_SHORT).show()
                }
                return@saveLesson
            }

            lessonRepository.replaceActivitiesForLesson(updatedLesson.id, activities) { activitiesSaved ->
                runOnUiThread {
                    restoreButtons()
                    if (!activitiesSaved) {
                        Toast.makeText(this, "Lesson saved, but some activities failed to update.", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    lesson = updatedLesson
                    binding.tvStatus.text = "Status: ${status.replaceFirstChar { it.uppercase() }}"
                    aiTouchedLesson = false
                    updateReleaseHint()
                    val releaseMessage = if (updatedLesson.releasedSectionIds.isEmpty()) {
                        "Lesson updated."
                    } else {
                        "Lesson updated for ${updatedLesson.releasedSectionIds.joinToString(", ")}."
                    }
                    Toast.makeText(this, releaseMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateReleaseHint() {
        val selectedSections = releaseCheckBoxes.filter { it.isChecked }.map { it.text.toString() }
        binding.tvReleaseHint.text = if (selectedSections.isEmpty()) {
            "Published lessons only appear for the sections released here."
        } else {
            "This lesson will appear for ${selectedSections.size} section(s): ${selectedSections.joinToString(", ")}."
        }
    }

    private fun restoreButtons() {
        binding.btnSaveDraft.isEnabled = true
        binding.btnSendReview.isEnabled = true
        binding.btnPublish.isEnabled = true
    }

    private fun generateLessonDraft() {
        binding.tvAiStatus.text = "Generating lesson draft..."
        val currentUser = storageService.getUser()
        lifecycleScope.launch {
            try {
                val draft = aiService.generateLessonDraft(
                    gradeLevel = lesson.gradeLevel.ifBlank { currentTeacherGrade() },
                    quarterTitle = lesson.quarterId,
                    unitTitle = binding.tvLessonLabel.text.toString(),
                    lessonTitleHint = binding.etTitle.text?.toString().orEmpty().ifBlank { lesson.title },
                    lessonObjectiveHint = binding.etObjective.text?.toString().orEmpty().ifBlank { lesson.objective }
                )
                binding.etTitle.setText(draft.title.ifBlank { binding.etTitle.text })
                binding.etObjective.setText(draft.objective.ifBlank { binding.etObjective.text })
                binding.etSummary.setText(draft.summary.ifBlank { binding.etSummary.text })
                aiTouchedLesson = true
                aiGovernanceService.saveDraftVariant(
                    targetType = "lesson",
                    targetId = lesson.id,
                    feature = "lesson_draft",
                    generatedText = draft.rawText,
                    createdBy = currentUser?.id.orEmpty(),
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION
                )
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "lesson_draft",
                    targetType = "lesson",
                    targetId = lesson.id,
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION,
                    status = "success"
                )
                binding.tvAiStatus.text = "Gemini drafted a lesson suggestion. Review it before saving."
            } catch (e: Exception) {
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "lesson_draft",
                    targetType = "lesson",
                    targetId = lesson.id,
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION,
                    status = "failed",
                    errorMessage = e.message.orEmpty()
                )
                binding.tvAiStatus.text = "Gemini draft failed: ${e.message ?: "unknown error"}"
            }
        }
    }

    private fun simplifyLesson() {
        binding.tvAiStatus.text = "Simplifying lesson..."
        val currentUser = storageService.getUser()
        lifecycleScope.launch {
            try {
                val draft = aiService.simplifyLesson(
                    gradeLevel = lesson.gradeLevel.ifBlank { currentTeacherGrade() },
                    title = binding.etTitle.text?.toString().orEmpty().ifBlank { lesson.title },
                    objective = binding.etObjective.text?.toString().orEmpty().ifBlank { lesson.objective },
                    summary = binding.etSummary.text?.toString().orEmpty().ifBlank { lesson.summary }
                )
                binding.etTitle.setText(draft.title.ifBlank { binding.etTitle.text })
                binding.etObjective.setText(draft.objective.ifBlank { binding.etObjective.text })
                binding.etSummary.setText(draft.summary.ifBlank { binding.etSummary.text })
                aiTouchedLesson = true
                aiGovernanceService.saveDraftVariant(
                    targetType = "lesson",
                    targetId = lesson.id,
                    feature = "lesson_simplify",
                    generatedText = draft.rawText,
                    createdBy = currentUser?.id.orEmpty(),
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION
                )
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "lesson_simplify",
                    targetType = "lesson",
                    targetId = lesson.id,
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION,
                    status = "success"
                )
                binding.tvAiStatus.text = "Gemini simplified the lesson text. Review it before saving."
            } catch (e: Exception) {
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "lesson_simplify",
                    targetType = "lesson",
                    targetId = lesson.id,
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION,
                    status = "failed",
                    errorMessage = e.message.orEmpty()
                )
                binding.tvAiStatus.text = "Gemini simplify failed: ${e.message ?: "unknown error"}"
            }
        }
    }

    private fun generateQuizActivities() {
        binding.tvAiStatus.text = "Generating formative activities..."
        val currentUser = storageService.getUser()
        lifecycleScope.launch {
            try {
                val activities = aiService.generateQuizActivities(
                    gradeLevel = lesson.gradeLevel.ifBlank { currentTeacherGrade() },
                    lessonTitle = binding.etTitle.text?.toString().orEmpty().ifBlank { lesson.title },
                    objective = binding.etObjective.text?.toString().orEmpty().ifBlank { lesson.objective },
                    summary = binding.etSummary.text?.toString().orEmpty().ifBlank { lesson.summary }
                )
                activities.forEach { suggestion ->
                    addActivityRow(
                        LessonActivity(
                            lessonId = lesson.id,
                            type = suggestion.type,
                            prompt = suggestion.prompt,
                            instructions = suggestion.instructions,
                            content = suggestion.instructions,
                            options = suggestion.options,
                            correctAnswer = suggestion.answer,
                            explanation = suggestion.answer
                        )
                    )
                }
                aiTouchedLesson = true
                val generatedText = activities.joinToString("\n\n") {
                    "TYPE: ${it.type}\nPROMPT: ${it.prompt}\nINSTRUCTIONS: ${it.instructions}\nANSWER: ${it.answer}"
                }
                aiGovernanceService.saveDraftVariant(
                    targetType = "lesson",
                    targetId = lesson.id,
                    feature = "quiz_generation",
                    generatedText = generatedText,
                    createdBy = currentUser?.id.orEmpty(),
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION
                )
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "quiz_generation",
                    targetType = "lesson",
                    targetId = lesson.id,
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION,
                    status = "success"
                )
                binding.tvAiStatus.text = "Gemini added ${activities.size} activity suggestions. Review them before saving."
            } catch (e: Exception) {
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "quiz_generation",
                    targetType = "lesson",
                    targetId = lesson.id,
                    modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
                    promptVersion = TeacherCopilotDefaults.PROMPT_VERSION,
                    status = "failed",
                    errorMessage = e.message.orEmpty()
                )
                binding.tvAiStatus.text = "Gemini quiz generation failed: ${e.message ?: "unknown error"}"
            }
        }
    }

    private fun currentTeacherGrade(): String {
        return SchoolStructure.normalizeGradeLevel(storageService.getUser()?.gradeLevel.orEmpty())
            .ifBlank { SchoolStructure.defaultGradeLevelForRole("teacher") }
    }
}
