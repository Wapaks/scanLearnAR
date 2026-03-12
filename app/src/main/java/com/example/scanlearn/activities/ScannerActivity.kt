package com.example.scanlearn.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.LearningObjectAdapter
import com.example.scanlearn.databinding.ActivityScannerBinding
import com.example.scanlearn.models.LearningData
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.ObjectClassifier

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private val classifier = ObjectClassifier()
    private var mode = AppConstants.MODE_EXPLORER

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
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            classifyAndNavigate(bitmap)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) classifyAndNavigate(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getStringExtra(AppConstants.EXTRA_MODE) ?: AppConstants.MODE_EXPLORER

        val modeColor = AppColors.getModeColor(mode)
        binding.toolbar.setBackgroundColor(modeColor)
        binding.tvToolbarTitle.text = AppColors.getModeLabel(mode)
        binding.ivCameraIcon.setColorFilter(modeColor)
        binding.btnCamera.setStrokeColor(ColorStateList.valueOf(modeColor))
        binding.ivCameraCardIcon.setColorFilter(modeColor)
        binding.btnGallery.setStrokeColor(ColorStateList.valueOf(modeColor))
        binding.ivGalleryIcon.setColorFilter(modeColor)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCamera.setOnClickListener { requestCameraOrOpen() }
        binding.btnGallery.setOnClickListener { imagePickerLauncher.launch("image/*") }

        setupObjectList()
    }

    private fun setupObjectList() {
        binding.rvObjects.layoutManager = LinearLayoutManager(this)
        binding.rvObjects.adapter = LearningObjectAdapter(LearningData.LEARNING_OBJECTS, mode) { obj ->
            openObjectDetail(obj , null)
        }
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

        classifier.classify(bitmap) { objectId ->
            runOnUiThread {
                if (objectId == null) {
                    Toast.makeText(
                        this,
                        "No recognizable object detected.\nTry scanning: apple, leaf, rock, water, or sky.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val obj = LearningData.LEARNING_OBJECTS.find { it.id == objectId }
                    if (obj != null) openObjectDetail(obj, bitmap)
                }
            }
        }
    }

    private fun openObjectDetail(obj: LearningObject, scannedBitmap: Bitmap? = null) {
        // Save bitmap to cache file so we can pass it to next activity
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