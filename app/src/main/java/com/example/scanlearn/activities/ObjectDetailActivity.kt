package com.example.scanlearn.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.scanlearn.adapters.FactsAdapter
import com.example.scanlearn.databinding.ActivityObjectDetailBinding
import com.example.scanlearn.models.LearningData
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants
import java.io.File

class ObjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityObjectDetailBinding
    private var showFacts = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val objectId = intent.getStringExtra(AppConstants.EXTRA_OBJECT_ID) ?: return
        val mode = intent.getStringExtra(AppConstants.EXTRA_MODE) ?: AppConstants.MODE_EXPLORER
        val hasScannedImage = intent.getBooleanExtra(AppConstants.EXTRA_HAS_SCANNED_IMAGE, false)
        val obj = LearningData.LEARNING_OBJECTS.find { it.id == objectId } ?: return

        val modeColor = AppColors.getModeColor(mode)
        binding.toolbar.setBackgroundColor(modeColor)
        binding.btnBack.setOnClickListener { finish() }

        // Load image — use scanned image if available, otherwise use URL
        if (hasScannedImage) {
            try {
                val cacheFile = File(cacheDir, "scanned_image.jpg")
                if (cacheFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                    binding.ivObject.setImageBitmap(bitmap)
                } else {
                    loadFromUrl(obj.imageUrl)
                }
            } catch (e: Exception) {
                loadFromUrl(obj.imageUrl)
            }
        } else {
            loadFromUrl(obj.imageUrl)
        }

        binding.tvObjectName.text = obj.name
        binding.tvCategory.text = obj.category.uppercase()
        binding.tvCategory.backgroundTintList =
            android.content.res.ColorStateList.valueOf(modeColor)
        binding.tvDescription.text = obj.description

        // Facts toggle
        binding.btnFacts.setBackgroundColor(modeColor)
        binding.rvFacts.layoutManager = LinearLayoutManager(this)
        binding.rvFacts.adapter = FactsAdapter(obj.facts)

        binding.btnFacts.setOnClickListener {
            showFacts = !showFacts
            binding.rvFacts.visibility = if (showFacts) View.VISIBLE else View.GONE
            binding.btnFacts.text = if (showFacts) "Hide Facts" else "Show Interesting Facts"
        }

        // Start quiz
        binding.btnStartQuiz.setBackgroundColor(modeColor)
        binding.btnStartQuiz.setOnClickListener {
            val intent = android.content.Intent(this, QuizActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_OBJECT_ID, obj.id)
            intent.putExtra(AppConstants.EXTRA_MODE, mode)
            startActivity(intent)
        }
    }

    private fun loadFromUrl(url: String) {
        Glide.with(this)
            .load(url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.ivObject)
    }
}