package com.example.scanlearn.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.LearningObjectAdapter
import com.example.scanlearn.databinding.ActivityRecognitionResultBinding
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.ObjectSelectionBox
import com.example.scanlearn.models.ScanAttempt
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecognitionResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecognitionResultBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService

    private var mode = AppConstants.MODE_EXPLORER
    private var missionId: String = ""
    private var suggestedIds: List<String> = emptyList()
    private var detectedCategory: String = "animals"
    private var scanConfidence: Float = 0f
    private var selectionApplied = false
    private var selectedBox = ObjectSelectionBox()
    private var allObjects: List<LearningObject> = emptyList()
    private var currentManualCategory: String = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecognitionResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()
        storageService = StorageService(this)

        mode = intent.getStringExtra(AppConstants.EXTRA_MODE) ?: AppConstants.MODE_EXPLORER
        missionId = intent.getStringExtra(AppConstants.EXTRA_MISSION_ID) ?: ""
        detectedCategory = intent.getStringExtra(AppConstants.EXTRA_SCAN_CATEGORY) ?: "animals"
        suggestedIds = intent.getStringArrayListExtra(AppConstants.EXTRA_SCAN_SUGGESTIONS) ?: emptyList()
        scanConfidence = intent.getFloatExtra(AppConstants.EXTRA_SCAN_CONFIDENCE, 0f)
        selectionApplied = intent.getBooleanExtra(AppConstants.EXTRA_SCAN_SELECTION_APPLIED, false)
        selectedBox =
            intent.getSerializableExtra(AppConstants.EXTRA_SCAN_SELECTION_BOX) as? ObjectSelectionBox
                ?: ObjectSelectionBox()
        currentManualCategory = detectedCategory

        val modeColor = AppColors.getModeColor(mode)
        binding.toolbar.setBackgroundColor(modeColor)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnOpenManual.setBackgroundColor(modeColor)

        bindPreview()
        bindRecognitionState()
        bindCategoryTabs()
        binding.btnOpenManual.setOnClickListener {
            binding.manualSection.visibility = View.VISIBLE
            binding.btnOpenManual.visibility = View.GONE
        }

        loadObjects()
    }

    private fun bindPreview() {
        val cacheFile = File(cacheDir, "scanned_image.jpg")
        if (cacheFile.exists()) {
            binding.ivScannedPreview.setImageBitmap(BitmapFactory.decodeFile(cacheFile.absolutePath))
        }
    }

    private fun bindRecognitionState() {
        val confidencePercent = (scanConfidence * 100).toInt()
        binding.tvDetectedCategory.text =
            "Detected category: ${detectedCategory.replaceFirstChar { it.uppercase() }}"

        val confidenceLabel = when {
            scanConfidence >= 0.8f -> "High confidence"
            scanConfidence >= 0.55f -> "Medium confidence"
            scanConfidence > 0f -> "Low confidence"
            else -> "No confident match"
        }
        binding.tvConfidence.text = "$confidenceLabel • $confidencePercent%"
        binding.tvRecognitionMessage.text = if (suggestedIds.isNotEmpty()) {
            if (selectionApplied) {
                "Choose the correct object from the selected area before we open the lesson."
            } else {
                "Choose the correct object before we open the lesson."
            }
        } else {
            if (selectionApplied) {
                "We checked the selected area but still are not sure. Please choose the correct object manually."
            } else {
                "We are not sure yet. Please choose the correct object manually."
            }
        }
    }

    private fun bindCategoryTabs() {
        binding.tabAll.setOnClickListener { showManualCategory("all") }
        binding.tabAnimals.setOnClickListener { showManualCategory("animals") }
        binding.tabPlants.setOnClickListener { showManualCategory("plants") }
        binding.tabClassroom.setOnClickListener { showManualCategory("classroom") }
    }

    private fun loadObjects() {
        dbService.getPublishedLearningObjects { objects ->
            allObjects = objects
            runOnUiThread {
                bindSuggestionList()
                showManualCategory(currentManualCategory)
            }
        }
    }

    private fun bindSuggestionList() {
        val suggestions = suggestedIds.mapNotNull { suggestionId ->
            allObjects.find { it.id == suggestionId }
        }
        val hasSuggestions = suggestions.isNotEmpty()

        binding.tvSuggestionsLabel.visibility = if (hasSuggestions) View.VISIBLE else View.GONE
        binding.rvSuggestions.visibility = if (hasSuggestions) View.VISIBLE else View.GONE
        binding.emptySuggestions.visibility = if (hasSuggestions) View.GONE else View.VISIBLE

        if (hasSuggestions) {
            binding.rvSuggestions.layoutManager = LinearLayoutManager(this)
            binding.rvSuggestions.adapter = LearningObjectAdapter(suggestions, mode) { obj ->
                confirmObject(obj, manualCorrection = false)
            }
        }
    }

    private fun showManualCategory(category: String) {
        currentManualCategory = category

        binding.tabAll.isSelected = category == "all"
        binding.tabAnimals.isSelected = category == "animals"
        binding.tabPlants.isSelected = category == "plants"
        binding.tabClassroom.isSelected = category == "classroom"

        val filtered = if (category == "all") {
            allObjects
        } else {
            allObjects.filter { it.category.equals(category, ignoreCase = true) }
        }

        binding.tvManualHint.text = if (category == "all") {
            "Choose any object from the learning library."
        } else {
            "Choose the correct object from ${category.replaceFirstChar { it.uppercase() }}."
        }
        binding.rvManualObjects.layoutManager = LinearLayoutManager(this)
        binding.rvManualObjects.adapter = LearningObjectAdapter(filtered, mode) { obj ->
            confirmObject(obj, manualCorrection = true)
        }
    }

    private fun confirmObject(obj: LearningObject, manualCorrection: Boolean) {
        val user = storageService.getUser()
        if (user == null) {
            Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressSaving.visibility = View.VISIBLE

        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        val attempt = ScanAttempt(
            studentId = user.id,
            mode = mode,
            categoryContext = detectedCategory,
            suggestions = suggestedIds,
            selectedObjectId = obj.id,
            confidence = scanConfidence,
            manualCorrection = manualCorrection,
            selectedBox = selectedBox,
            selectionApplied = selectionApplied,
            createdAt = timestamp
        )

        dbService.saveScanAttempt(user.id, attempt) { attemptId ->
            runOnUiThread {
                binding.progressSaving.visibility = View.GONE

                if (attemptId == null) {
                    Toast.makeText(this, "Could not save the scan result. Please try again.", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                val intent = Intent(this, ObjectDetailActivity::class.java)
                intent.putExtra(AppConstants.EXTRA_OBJECT_ID, obj.id)
                intent.putExtra(AppConstants.EXTRA_MODE, mode)
                intent.putExtra(AppConstants.EXTRA_MISSION_ID, missionId)
                intent.putExtra(AppConstants.EXTRA_HAS_SCANNED_IMAGE, true)
                intent.putExtra(AppConstants.EXTRA_SCAN_ATTEMPT_ID, attemptId)
                intent.putExtra(AppConstants.EXTRA_SCAN_CONFIDENCE, scanConfidence)
                intent.putExtra(AppConstants.EXTRA_SCAN_MANUAL_CORRECTION, manualCorrection)
                startActivity(intent)
                finish()
            }
        }
    }
}
