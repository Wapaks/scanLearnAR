package com.example.scanlearn.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.LearningObjectAdapter
import com.example.scanlearn.databinding.ActivityScannerBinding
import com.example.scanlearn.models.ClassificationResult
import com.example.scanlearn.models.DetectionLabel
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.ObjectClassifier

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private val classifier = ObjectClassifier()
    private var mode = AppConstants.MODE_EXPLORER
    private var missionId: String = ""
    private lateinit var dbService: RealtimeDbService

    private var allObjects: List<LearningObject> = emptyList()
    private var currentCategory = "animals"
    private var objectsReady = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (!objectsReady) {
                Toast.makeText(this, "Still loading objects, please wait...", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            classifyAndNavigate(bitmap)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            if (!objectsReady) {
                Toast.makeText(this, "Still loading objects, please wait...", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            classifyAndNavigate(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getStringExtra(AppConstants.EXTRA_MODE) ?: AppConstants.MODE_EXPLORER
        missionId = intent.getStringExtra(AppConstants.EXTRA_MISSION_ID) ?: ""
        dbService = RealtimeDbService()

        val modeColor = AppColors.getModeColor(mode)
        binding.toolbar.setBackgroundColor(modeColor)
        binding.tvToolbarTitle.text = AppColors.getModeLabel(mode)
        binding.ivCameraIcon.setColorFilter(modeColor)
        binding.btnCamera.setStrokeColor(ColorStateList.valueOf(modeColor))
        binding.ivCameraCardIcon.setColorFilter(modeColor)
        binding.btnGallery.setStrokeColor(ColorStateList.valueOf(modeColor))
        binding.ivGalleryIcon.setColorFilter(modeColor)

        binding.btnBack.setOnClickListener { finish() }
        setScanButtonsEnabled(false)

        binding.btnCamera.setOnClickListener {
            if (!objectsReady) {
                Toast.makeText(this, "Still loading objects, please wait...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            requestCameraOrOpen()
        }
        binding.btnGallery.setOnClickListener {
            if (!objectsReady) {
                Toast.makeText(this, "Still loading objects, please wait...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            imagePickerLauncher.launch("image/*")
        }

        binding.tabAnimals.setOnClickListener { showCategory("animals") }
        binding.tabPlants.setOnClickListener { showCategory("plants") }
        binding.tabClassroom.setOnClickListener { showCategory("classroom") }

        loadObjects()
    }

    private fun loadObjects() {
        binding.rvObjects.visibility = View.GONE
        binding.objectsLoadingIndicator.visibility = View.VISIBLE

        dbService.getPublishedLearningObjects { objects ->
            allObjects = objects
            objectsReady = true
            runOnUiThread {
                binding.objectsLoadingIndicator.visibility = View.GONE
                setScanButtonsEnabled(true)
                showCategory(currentCategory)
            }
        }
    }

    private fun showCategory(category: String) {
        currentCategory = category

        binding.tabAnimals.isSelected = category == "animals"
        binding.tabPlants.isSelected = category == "plants"
        binding.tabClassroom.isSelected = category == "classroom"

        val filtered = allObjects.filter { it.category.lowercase() == category }
        binding.rvObjects.visibility = View.VISIBLE
        binding.rvObjects.layoutManager = LinearLayoutManager(this)
        binding.rvObjects.adapter = LearningObjectAdapter(filtered, mode) { obj ->
            openObjectDetail(obj.id, hasScannedImage = false)
        }
    }

    private fun setScanButtonsEnabled(enabled: Boolean) {
        binding.btnCamera.alpha = if (enabled) 1.0f else 0.5f
        binding.btnGallery.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun requestCameraOrOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> openCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    private fun classifyAndNavigate(bitmap: Bitmap) {
        Toast.makeText(this, "Analyzing image...", Toast.LENGTH_SHORT).show()

        classifier.classify(bitmap) { result ->
            runOnUiThread {
                if (result.labels.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Could not detect the object clearly. Please choose manually.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                openRecognitionResult(bitmap, result)
            }
        }
    }

    private fun openRecognitionResult(bitmap: Bitmap, result: ClassificationResult) {
        cacheScannedBitmap(bitmap)

        val resolvedCategory = result.category ?: currentCategory
        val suggestions = resolveSuggestions(
            category = resolvedCategory,
            specificId = result.specificId,
            detectedLabels = result.labels
        )

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
        startActivity(intent)
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

    private fun resolveObject(
        category: String,
        specificId: String?,
        mlKitLabels: List<String>
    ): LearningObject? {
        val categoryObjects = allObjects.filter { it.category.lowercase() == category.lowercase() }

        // Layer 1 — exact specific ID match (e.g. id == "dog")
        if (specificId != null) {
            val byId = categoryObjects.find { it.id == specificId }
            if (byId != null) return byId
        }

        // Layer 2 — exact name match against MLKit labels (e.g. MLKit "Fish" == name "Fish")
        for (label in mlKitLabels) {
            val byName = categoryObjects.find { it.name.lowercase() == label.lowercase() }
            if (byName != null) return byName
        }

        // Layer 3 — expanded dictionary maps label to a general pool name
        // e.g. MLKit "Shark" → labelToGeneralName["shark"] = "Fish" → find name=="Fish"
        val generalName = classifier.resolveGeneralName(mlKitLabels)
        if (generalName != null) {
            val byGeneralName = categoryObjects.find {
                it.name.lowercase() == generalName.lowercase()
            }
            if (byGeneralName != null) return byGeneralName
        }

        // Layer 4 — random from general pool IDs (animal_general_1 ... animal_general_5)
        val generalIds = (1..5).map { "${category}_general_$it" }
        val generals = categoryObjects.filter { it.id in generalIds }
        if (generals.isNotEmpty()) return generals.random()

        // Layer 5 — last resort: any object in the category
        return categoryObjects.randomOrNull()
    }

    private fun cacheScannedBitmap(scannedBitmap: Bitmap) {
        try {
            val cacheFile = java.io.File(cacheDir, "scanned_image.jpg")
            val fos = java.io.FileOutputStream(cacheFile)
            scannedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openObjectDetail(objectId: String, hasScannedImage: Boolean) {
        val intent = Intent(this, ObjectDetailActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_OBJECT_ID, objectId)
        intent.putExtra(AppConstants.EXTRA_MODE, mode)
        intent.putExtra(AppConstants.EXTRA_MISSION_ID, missionId)
        intent.putExtra(AppConstants.EXTRA_HAS_SCANNED_IMAGE, hasScannedImage)
        startActivity(intent)
    }

    private fun openObjectDetail(obj: LearningObject, scannedBitmap: Bitmap? = null) {
        if (scannedBitmap != null) {
            try {
                val cacheFile = java.io.File(cacheDir, "scanned_image.jpg")
                val fos = java.io.FileOutputStream(cacheFile)
                scannedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val intent = Intent(this, ObjectDetailActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_OBJECT_ID, obj.id)
        intent.putExtra(AppConstants.EXTRA_MODE, mode)
        intent.putExtra(AppConstants.EXTRA_HAS_SCANNED_IMAGE, scannedBitmap != null)
        startActivity(intent)
    }
}
