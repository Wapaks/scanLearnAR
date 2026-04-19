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
import com.example.scanlearn.activities.ObjectSelectionActivity.Companion.CROPPED_SCAN_FILE
import com.example.scanlearn.activities.ObjectSelectionActivity.Companion.ORIGINAL_SCAN_FILE
import com.example.scanlearn.adapters.LearningObjectAdapter
import com.example.scanlearn.databinding.ActivityScannerBinding
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
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
            openObjectSelection(bitmap)
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
            openObjectSelection(bitmap)
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

    private fun openObjectSelection(bitmap: Bitmap) {
        cacheScannedBitmap(bitmap)
        val intent = Intent(this, ObjectSelectionActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_MODE, mode)
        intent.putExtra(AppConstants.EXTRA_MISSION_ID, missionId)
        intent.putExtra(AppConstants.EXTRA_SCAN_CATEGORY, currentCategory)
        startActivity(intent)
    }

    private fun cacheScannedBitmap(scannedBitmap: Bitmap) {
        try {
            val originalFile = java.io.File(cacheDir, ORIGINAL_SCAN_FILE)
            val workingFile = java.io.File(cacheDir, CROPPED_SCAN_FILE)
            java.io.FileOutputStream(originalFile).use { fos ->
                scannedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }
            java.io.FileOutputStream(workingFile).use { fos ->
                scannedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }
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
}
