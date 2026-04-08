package com.example.scanlearn.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.scanlearn.databinding.ActivityAddObjectBinding
import com.example.scanlearn.databinding.ItemQuizEditorBinding
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.QuizQuestion
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.ObjectClassifier
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddObjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddObjectBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService
    private val classifier = ObjectClassifier()
    private var scannedBitmap: Bitmap? = null
    private var detectedCategory: String? = null
    private var editingObjectId: String = ""
    private var currentObjectStatus: String = "published"
    private var currentCreatedAt: String = ""
    private var currentCreatedBy: String = ""
    private var currentImageUrl: String = ""
    private val quizBindings = mutableListOf<ItemQuizEditorBinding>()

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
        else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            processImage(bitmap)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { processImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddObjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()
        storageService = StorageService(this)
        editingObjectId = intent.getStringExtra(AppConstants.EXTRA_TEACHER_OBJECT_ID) ?: ""

        binding.btnBack.setOnClickListener { finish() }
        binding.btnChooseCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                cameraLauncher.launch(null)
            else
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        binding.btnChooseGallery.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.btnAddQuiz.setOnClickListener { addQuizRow() }
        binding.btnSave.setOnClickListener { handleSave() }

        binding.formSection.visibility = View.GONE

        if (editingObjectId.isNotBlank()) {
            binding.tvDetecting.visibility = View.GONE
            binding.formSection.visibility = View.VISIBLE
            binding.btnSave.text = "Update Object"
            loadExistingObject()
        }
    }

    private fun processImage(bitmap: Bitmap) {
        scannedBitmap = bitmap
        binding.ivPreview.setImageBitmap(bitmap)
        binding.tvDetecting.visibility = View.VISIBLE
        binding.formSection.visibility = View.GONE

        val image = InputImage.fromBitmap(bitmap, 0)
        val options = ImageLabelerOptions.Builder().setConfidenceThreshold(0.5f).build()
        val labeler = ImageLabeling.getClient(options)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                val labelTexts = labels.map { it.text }
                val category = classifier.detectCategory(labelTexts)
                detectedCategory = category
                dbService.getLearningObjects { objects ->
                    val template = findSuggestedTemplate(objects, labelTexts, category)
                    runOnUiThread {
                        binding.tvDetecting.visibility = View.GONE
                        if (category == null && template == null) {
                            Toast.makeText(this, "Could not detect a matching object. Please fill in manually.", Toast.LENGTH_LONG).show()
                        } else {
                            val matchType = if (template != null) {
                                "Matched: ${template.name}"
                            } else {
                                "Category: ${(category ?: "").replaceFirstChar { it.uppercase() }}"
                            }
                            Toast.makeText(this, "Detected - $matchType. Edit details below.", Toast.LENGTH_LONG).show()
                        }
                        showForm(template, category)
                    }
                }
                labeler.close()
            }
            .addOnFailureListener {
                runOnUiThread {
                    binding.tvDetecting.visibility = View.GONE
                    showForm(null, null)
                }
                labeler.close()
            }
    }

    private fun findSuggestedTemplate(
        objects: List<LearningObject>,
        labels: List<String>,
        category: String?
    ): LearningObject? {
        val candidatePool = if (category.isNullOrBlank()) {
            objects
        } else {
            objects.filter { it.category.equals(category, ignoreCase = true) }
        }

        if (candidatePool.isEmpty()) return null

        for (label in labels) {
            val normalized = label.lowercase()
            candidatePool.find { it.id.equals(normalized, ignoreCase = true) }?.let { return it }
            candidatePool.find { it.name.equals(label, ignoreCase = true) }?.let { return it }
        }

        val generalName = classifier.resolveGeneralName(labels)
        if (generalName != null) {
            candidatePool.find { it.name.equals(generalName, ignoreCase = true) }?.let { return it }
        }

        return null
    }

    private fun showForm(template: LearningObject?, category: String?) {
        binding.formSection.visibility = View.VISIBLE
        binding.etObjectName.setText(template?.name ?: "")
        binding.etCategory.setText(category?.replaceFirstChar { it.uppercase() } ?: template?.category ?: "")
        binding.etDescription.setText(template?.description ?: "")
        binding.etFact1.setText(template?.facts?.getOrNull(0) ?: "")
        binding.etFact2.setText(template?.facts?.getOrNull(1) ?: "")
        binding.etFact3.setText(template?.facts?.getOrNull(2) ?: "")
        binding.etFact4.setText(template?.facts?.getOrNull(3) ?: "")
        binding.etFact5.setText(template?.facts?.getOrNull(4) ?: "")

        binding.quizContainer.removeAllViews()
        quizBindings.clear()

        val quizList = template?.quiz ?: emptyList()
        if (quizList.isEmpty()) repeat(3) { addQuizRow() }
        else quizList.forEach { addQuizRow(it) }
    }

    private fun loadExistingObject() {
        binding.saveProgress.visibility = View.VISIBLE
        dbService.getLearningObject(editingObjectId) { objectItem ->
            runOnUiThread {
                binding.saveProgress.visibility = View.GONE
                if (objectItem == null) {
                    Toast.makeText(this, "Could not load this object.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@runOnUiThread
                }

                currentObjectStatus = objectItem.status.ifBlank { "published" }
                currentCreatedAt = objectItem.createdAt
                currentCreatedBy = objectItem.createdBy
                currentImageUrl = objectItem.imageUrl
                showForm(objectItem, objectItem.category)
            }
        }
    }

    private fun addQuizRow(prefill: QuizQuestion? = null) {
        val qb = ItemQuizEditorBinding.inflate(LayoutInflater.from(this), binding.quizContainer, false)

        qb.etQuestion.setText(prefill?.question ?: "")
        qb.etOption1.setText(prefill?.options?.getOrNull(0) ?: "")
        qb.etOption2.setText(prefill?.options?.getOrNull(1) ?: "")
        qb.etOption3.setText(prefill?.options?.getOrNull(2) ?: "")
        qb.etOption4.setText(prefill?.options?.getOrNull(3) ?: "")

        listOf(qb.rb1, qb.rb2, qb.rb3, qb.rb4)
            .getOrNull(prefill?.correctAnswer ?: 0)?.isChecked = true

        qb.btnRemoveQuiz.setOnClickListener {
            binding.quizContainer.removeView(qb.root)
            quizBindings.remove(qb)
        }

        binding.quizContainer.addView(qb.root)
        quizBindings.add(qb)
    }

    private fun handleSave() {
        val name = binding.etObjectName.text.toString().trim()
        val category = binding.etCategory.text.toString().trim().lowercase()
        val description = binding.etDescription.text.toString().trim()

        val facts = listOf(
            binding.etFact1.text.toString().trim(),
            binding.etFact2.text.toString().trim(),
            binding.etFact3.text.toString().trim(),
            binding.etFact4.text.toString().trim(),
            binding.etFact5.text.toString().trim()
        ).filter { it.isNotEmpty() }

        if (name.isEmpty()) { binding.tilObjectName.error = "Name is required"; return }
        if (category.isEmpty()) { binding.tilCategory.error = "Category is required"; return }
        if (description.isEmpty()) { binding.tilDescription.error = "Description is required"; return }
        if (facts.isEmpty()) { Toast.makeText(this, "Add at least one fact", Toast.LENGTH_SHORT).show(); return }

        binding.tilObjectName.error = null
        binding.tilCategory.error = null
        binding.tilDescription.error = null

        val quiz = mutableListOf<QuizQuestion>()
        for (qb in quizBindings) {
            val question = qb.etQuestion.text.toString().trim()
            val o1 = qb.etOption1.text.toString().trim()
            val o2 = qb.etOption2.text.toString().trim()
            val o3 = qb.etOption3.text.toString().trim()
            val o4 = qb.etOption4.text.toString().trim()

            if (question.isEmpty() || o1.isEmpty() || o2.isEmpty() || o3.isEmpty() || o4.isEmpty()) {
                Toast.makeText(this, "Please fill in all quiz fields", Toast.LENGTH_SHORT).show()
                return
            }

            val correct = when {
                qb.rb1.isChecked -> 0
                qb.rb2.isChecked -> 1
                qb.rb3.isChecked -> 2
                qb.rb4.isChecked -> 3
                else -> { Toast.makeText(this, "Select a correct answer for each question", Toast.LENGTH_SHORT).show(); return }
            }
            quiz.add(QuizQuestion(question = question, options = listOf(o1, o2, o3, o4), correctAnswer = correct))
        }

        if (quiz.isEmpty()) { Toast.makeText(this, "Add at least one quiz question", Toast.LENGTH_SHORT).show(); return }

        binding.btnSave.isEnabled = false
        binding.saveProgress.visibility = View.VISIBLE

        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        val userId = storageService.getUser()?.id ?: currentCreatedBy
        val objectId = editingObjectId.ifBlank {
            name.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis()
        }
        val imageUrl = scannedBitmap?.let { bitmapToBase64(it) } ?: currentImageUrl

        val obj = LearningObject(
            id = objectId,
            name = name,
            category = category,
            description = description,
            imageUrl = imageUrl,
            facts = facts,
            quiz = quiz,
            status = currentObjectStatus,
            createdBy = currentCreatedBy.ifBlank { userId },
            createdAt = currentCreatedAt.ifBlank { now },
            updatedAt = now
        )

        dbService.saveLearningObject(obj) { success ->
            runOnUiThread {
                binding.btnSave.isEnabled = true
                binding.saveProgress.visibility = View.GONE
                if (success) {
                    val message = if (editingObjectId.isBlank()) {
                        "$name added! Students can now scan it."
                    } else {
                        "$name updated successfully."
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save. Check connection and try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return "data:image/jpeg;base64," + Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }
}
