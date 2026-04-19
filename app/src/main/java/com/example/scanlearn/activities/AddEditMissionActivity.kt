package com.example.scanlearn.activities

import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scanlearn.databinding.ActivityAddEditMissionBinding
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.Mission
import com.example.scanlearn.services.AiGovernanceService
import com.example.scanlearn.services.GeminiTeacherCopilotService
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class AddEditMissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditMissionBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService
    private val aiService = GeminiTeacherCopilotService()
    private val aiGovernanceService = AiGovernanceService()

    private var editingMissionId: String = ""
    private var currentMission: Mission? = null
    private var publishedObjects: List<LearningObject> = emptyList()
    private var aiTouchedMission = false

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
        dbService.getPublishedLearningObjects { objects ->
            publishedObjects = objects.sortedBy { it.name }

            if (editingMissionId.isBlank()) {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.tvScreenTitle.text = "Create Mission"
                    renderObjectOptions(emptyList())
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

    private fun bindMission(mission: Mission) {
        binding.etMissionTitle.setText(mission.title)
        binding.etMissionDescription.setText(mission.description)
        binding.etCategory.setText(mission.category)
        binding.cbSantan.isChecked = mission.sectionIds.any { it.equals("Santan", ignoreCase = true) }
        binding.cbDaisy.isChecked = mission.sectionIds.any { it.equals("Daisy", ignoreCase = true) }
        binding.cbOrchid.isChecked = mission.sectionIds.any { it.equals("Orchid", ignoreCase = true) }
        binding.switchActive.isChecked = mission.active
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

    private fun saveMission() {
        val title = binding.etMissionTitle.text.toString().trim()
        val description = binding.etMissionDescription.text.toString().trim()
        val category = binding.etCategory.text.toString().trim().lowercase()
        val sectionIds = buildList {
            if (binding.cbSantan.isChecked) add("Santan")
            if (binding.cbDaisy.isChecked) add("Daisy")
            if (binding.cbOrchid.isChecked) add("Orchid")
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
        binding.btnSaveMission.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE

        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        val currentUserId = storageService.getUser()?.id ?: ""
        val existing = currentMission

        val mission = Mission(
            id = existing?.id ?: "mission_${System.currentTimeMillis()}",
            title = title,
            description = description,
            objectsToFind = selectedObjectIds,
            sectionIds = sectionIds,
            category = category,
            active = binding.switchActive.isChecked,
            createdBy = existing?.createdBy?.ifBlank { currentUserId } ?: currentUserId,
            createdAt = existing?.createdAt?.ifBlank { now } ?: now,
            updatedAt = now,
            aiGenerated = existing?.aiGenerated == true || aiTouchedMission,
            aiSource = if (existing?.aiGenerated == true || aiTouchedMission) GeminiTeacherCopilotService.DEFAULT_MODEL else existing?.aiSource.orEmpty(),
            aiPromptVersion = if (existing?.aiGenerated == true || aiTouchedMission) GeminiTeacherCopilotService.PROMPT_VERSION else existing?.aiPromptVersion.orEmpty(),
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
            if (binding.cbSantan.isChecked) add("Santan")
            if (binding.cbDaisy.isChecked) add("Daisy")
            if (binding.cbOrchid.isChecked) add("Orchid")
        }

        binding.tvAiMissionStatus.text = "Generating mission draft..."
        lifecycleScope.launch {
            try {
                val suggestion = aiService.generateMissionDraft(
                    gradeLevel = storageService.getUser()?.gradeLevel?.ifBlank { "Grade 3" } ?: "Grade 3",
                    sectionNames = sectionIds.ifEmpty { listOf("Santan") },
                    category = binding.etCategory.text?.toString().orEmpty().ifBlank { "science" },
                    objectNames = selectedObjectNames.ifEmpty { publishedObjects.take(3).map { it.name } }
                )
                binding.etMissionTitle.setText(suggestion.title.ifBlank { binding.etMissionTitle.text })
                binding.etMissionDescription.setText(
                    suggestion.description.ifBlank { binding.etMissionDescription.text }
                )
                if (binding.etCategory.text.isNullOrBlank()) {
                    binding.etCategory.setText(suggestion.category)
                }
                aiTouchedMission = true
                aiGovernanceService.saveDraftVariant(
                    targetType = "mission",
                    targetId = currentMission?.id ?: editingMissionId.ifBlank { "new_mission" },
                    feature = "mission_generation",
                    generatedText = suggestion.rawText,
                    createdBy = currentUser?.id.orEmpty(),
                    modelName = GeminiTeacherCopilotService.DEFAULT_MODEL,
                    promptVersion = GeminiTeacherCopilotService.PROMPT_VERSION
                )
                aiGovernanceService.logUsage(
                    userId = currentUser?.id.orEmpty(),
                    role = currentUser?.role.orEmpty(),
                    feature = "mission_generation",
                    targetType = "mission",
                    targetId = currentMission?.id ?: editingMissionId.ifBlank { "new_mission" },
                    modelName = GeminiTeacherCopilotService.DEFAULT_MODEL,
                    promptVersion = GeminiTeacherCopilotService.PROMPT_VERSION,
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
                    modelName = GeminiTeacherCopilotService.DEFAULT_MODEL,
                    promptVersion = GeminiTeacherCopilotService.PROMPT_VERSION,
                    status = "failed",
                    errorMessage = e.message.orEmpty()
                )
                binding.tvAiMissionStatus.text = "Gemini mission draft failed: ${e.message ?: "unknown error"}"
            }
        }
    }
}
