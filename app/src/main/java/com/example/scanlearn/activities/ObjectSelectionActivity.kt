package com.example.scanlearn.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scanlearn.databinding.ActivityObjectSelectionBinding
import com.example.scanlearn.models.ClassificationResult
import com.example.scanlearn.models.DetectionLabel
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.ObjectSelectionBox
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.ObjectClassifier
import com.example.scanlearn.utils.ObjectDetectorHelper
import java.io.File
import java.io.FileOutputStream

class ObjectSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityObjectSelectionBinding
    private val detectorHelper = ObjectDetectorHelper()
    private val classifier = ObjectClassifier()
    private lateinit var dbService: RealtimeDbService
    private var mode = AppConstants.MODE_EXPLORER
    private var missionId: String = ""
    private var initialCategory: String = "animals"
    private var sourceBitmap: Bitmap? = null
    private var selectedRect: Rect? = null
    private var allObjects: List<LearningObject> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getStringExtra(AppConstants.EXTRA_MODE) ?: AppConstants.MODE_EXPLORER
        missionId = intent.getStringExtra(AppConstants.EXTRA_MISSION_ID) ?: ""
        initialCategory = intent.getStringExtra(AppConstants.EXTRA_SCAN_CATEGORY) ?: "animals"
        dbService = RealtimeDbService()

        val modeColor = AppColors.getModeColor(mode)
        binding.toolbar.setBackgroundColor(modeColor)
        binding.btnContinue.setBackgroundColor(modeColor)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSkipSelection.setOnClickListener {
            prepareImageAndAnalyze(selectionApplied = false, selectionBox = null)
        }
        binding.btnContinue.setOnClickListener {
            val selected = selectedRect
            if (selected == null) {
                Toast.makeText(this, "Please tap the object you want first.", Toast.LENGTH_SHORT).show()
            } else {
                prepareImageAndAnalyze(
                    selectionApplied = true,
                    selectionBox = ObjectSelectionBox(
                        left = selected.left,
                        top = selected.top,
                        right = selected.right,
                        bottom = selected.bottom
                    )
                )
            }
        }

        loadPreviewAndDetect()
    }

    private fun loadPreviewAndDetect() {
        val cacheFile = File(cacheDir, ORIGINAL_SCAN_FILE)
        if (!cacheFile.exists()) {
            Toast.makeText(this, "Image not found. Please try scanning again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        sourceBitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
        val bitmap = sourceBitmap
        if (bitmap == null) {
            Toast.makeText(this, "Could not load the image.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.ivPreview.setImageBitmap(bitmap)
        binding.selectionOverlay.bindImageSize(bitmap.width, bitmap.height)
        binding.selectionOverlay.setOnBoxSelectedListener { rect ->
            selectedRect = rect
            binding.btnContinue.isEnabled = true
            binding.tvDetectionStatus.text = "Object selected. Continue to classify only that part of the image."
        }

        detectorHelper.detectObjects(bitmap) { detectedBoxes ->
            runOnUiThread {
                binding.progressDetection.visibility = android.view.View.GONE
                if (detectedBoxes.isEmpty()) {
                    binding.tvDetectionStatus.text =
                        "No separate object boxes were found. You can continue with the full image."
                    binding.btnContinue.isEnabled = false
                    binding.selectionOverlay.setBoxes(emptyList())
                } else {
                    binding.tvDetectionStatus.text =
                        "${detectedBoxes.size} object area(s) found. Tap the one you want to use."
                    binding.selectionOverlay.setBoxes(detectedBoxes)
                }
            }
        }
    }

    private fun saveCroppedBitmap(selected: Rect) {
        val bitmap = sourceBitmap ?: return
        val safeLeft = selected.left.coerceIn(0, bitmap.width - 1)
        val safeTop = selected.top.coerceIn(0, bitmap.height - 1)
        val safeRight = selected.right.coerceIn(safeLeft + 1, bitmap.width)
        val safeBottom = selected.bottom.coerceIn(safeTop + 1, bitmap.height)
        val cropWidth = safeRight - safeLeft
        val cropHeight = safeBottom - safeTop

        val cropped = Bitmap.createBitmap(bitmap, safeLeft, safeTop, cropWidth, cropHeight)
        val cacheFile = File(cacheDir, CROPPED_SCAN_FILE)
        FileOutputStream(cacheFile).use { output ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
    }

    private fun prepareImageAndAnalyze(
        selectionApplied: Boolean,
        selectionBox: ObjectSelectionBox?
    ) {
        if (selectionApplied) {
            val selected = selectedRect ?: return
            saveCroppedBitmap(selected)
        } else {
            copyOriginalToWorkingScan()
        }

        setBusyState(true)

        if (allObjects.isEmpty()) {
            dbService.getPublishedLearningObjects { objects ->
                allObjects = objects
                val bitmap = BitmapFactory.decodeFile(File(cacheDir, CROPPED_SCAN_FILE).absolutePath)
                if (bitmap == null) {
                    runOnUiThread {
                        setBusyState(false)
                        Toast.makeText(this, "Could not analyze the selected image.", Toast.LENGTH_SHORT).show()
                    }
                    return@getPublishedLearningObjects
                }
                classifyAndContinue(bitmap, selectionApplied, selectionBox)
            }
        } else {
            val bitmap = BitmapFactory.decodeFile(File(cacheDir, CROPPED_SCAN_FILE).absolutePath)
            if (bitmap == null) {
                setBusyState(false)
                Toast.makeText(this, "Could not analyze the selected image.", Toast.LENGTH_SHORT).show()
                return
            }
            classifyAndContinue(bitmap, selectionApplied, selectionBox)
        }
    }

    private fun classifyAndContinue(
        bitmap: Bitmap,
        selectionApplied: Boolean,
        selectionBox: ObjectSelectionBox?
    ) {
        classifier.classify(bitmap) { result ->
            val resolvedCategory = result.category ?: initialCategory
            val suggestions = resolveSuggestions(
                category = resolvedCategory,
                specificId = result.specificId,
                detectedLabels = result.labels
            )

            runOnUiThread {
                setBusyState(false)
                val intent = Intent(this, RecognitionResultActivity::class.java)
                intent.putExtra(AppConstants.EXTRA_MODE, mode)
                intent.putExtra(AppConstants.EXTRA_MISSION_ID, missionId)
                intent.putExtra(AppConstants.EXTRA_SCAN_CATEGORY, resolvedCategory)
                intent.putStringArrayListExtra(
                    AppConstants.EXTRA_SCAN_SUGGESTIONS,
                    ArrayList(suggestions.map { it.id })
                )
                intent.putExtra(
                    AppConstants.EXTRA_SCAN_CONFIDENCE,
                    result.labels.maxOfOrNull { it.confidence } ?: 0f
                )
                intent.putExtra(AppConstants.EXTRA_SCAN_SELECTION_APPLIED, selectionApplied)
                if (selectionBox != null) {
                    intent.putExtra(AppConstants.EXTRA_SCAN_SELECTION_BOX, selectionBox)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun resolveSuggestions(
        category: String,
        specificId: String?,
        detectedLabels: List<DetectionLabel>
    ): List<LearningObject> {
        val categoryObjects = allObjects.filter { it.category.equals(category, ignoreCase = true) }
        val candidatePool = if (categoryObjects.isNotEmpty()) categoryObjects else allObjects
        val scores = linkedMapOf<String, Float>()
        val labelTexts = detectedLabels.map { it.text }

        if (specificId != null) {
            candidatePool.find { it.id == specificId }?.let { exactObject ->
                scores[exactObject.id] = (scores[exactObject.id] ?: 0f) + 100f
            }
        }

        for (label in detectedLabels) {
            val lowerLabel = label.text.lowercase()
            candidatePool.forEach { obj ->
                val objectName = obj.name.lowercase()
                val objectId = obj.id.lowercase()
                when {
                    lowerLabel == objectName ->
                        scores[obj.id] = (scores[obj.id] ?: 0f) + 80f + label.confidence
                    lowerLabel == objectId ->
                        scores[obj.id] = (scores[obj.id] ?: 0f) + 85f + label.confidence
                    objectName.contains(lowerLabel) || lowerLabel.contains(objectName) ->
                        scores[obj.id] = (scores[obj.id] ?: 0f) + 45f + label.confidence
                    objectId.contains(lowerLabel) || lowerLabel.contains(objectId) ->
                        scores[obj.id] = (scores[obj.id] ?: 0f) + 40f + label.confidence
                }
            }
        }

        classifier.resolveGeneralName(labelTexts)?.let { generalName ->
            candidatePool.find { it.name.equals(generalName, ignoreCase = true) }?.let { generalObject ->
                scores[generalObject.id] = (scores[generalObject.id] ?: 0f) + 50f
            }
        }

        return candidatePool
            .filter { (scores[it.id] ?: 0f) > 0f }
            .sortedByDescending { scores[it.id] ?: 0f }
            .take(3)
    }

    private fun copyOriginalToWorkingScan() {
        val originalFile = File(cacheDir, ORIGINAL_SCAN_FILE)
        val workingFile = File(cacheDir, CROPPED_SCAN_FILE)
        if (originalFile.exists()) {
            originalFile.copyTo(workingFile, overwrite = true)
        }
    }

    private fun setBusyState(isBusy: Boolean) {
        binding.progressDetection.visibility = if (isBusy) View.VISIBLE else View.GONE
        binding.btnContinue.isEnabled = !isBusy && selectedRect != null
        binding.btnSkipSelection.isEnabled = !isBusy
        binding.selectionOverlay.isEnabled = !isBusy
        if (isBusy) {
            binding.tvDetectionStatus.text = "Analyzing the selected object..."
        }
    }

    companion object {
        const val ORIGINAL_SCAN_FILE = "scanned_image_original.jpg"
        const val CROPPED_SCAN_FILE = "scanned_image.jpg"
    }
}
