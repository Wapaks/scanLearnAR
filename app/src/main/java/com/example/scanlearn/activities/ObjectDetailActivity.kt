package com.example.scanlearn.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.scanlearn.adapters.FactsAdapter
import com.example.scanlearn.databinding.ActivityObjectDetailBinding
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.utils.AppColors
import com.example.scanlearn.utils.AppConstants
import java.io.File

class ObjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityObjectDetailBinding
    private lateinit var dbService: RealtimeDbService
    private var showFacts = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()

        val objectId = intent.getStringExtra(AppConstants.EXTRA_OBJECT_ID) ?: return
        val mode = intent.getStringExtra(AppConstants.EXTRA_MODE) ?: AppConstants.MODE_EXPLORER
        val hasScannedImage = intent.getBooleanExtra(AppConstants.EXTRA_HAS_SCANNED_IMAGE, false)
        val modeColor = AppColors.getModeColor(mode)

        binding.toolbar.setBackgroundColor(modeColor)
        binding.btnBack.setOnClickListener { finish() }

        dbService.getLearningObjects { objects ->
            val obj = objects.find { it.id == objectId }
            runOnUiThread {
                if (obj != null) bindObject(obj, mode, modeColor, hasScannedImage)
                else finish()
            }
        }
    }

    private fun bindObject(obj: LearningObject, mode: String, modeColor: Int, hasScannedImage: Boolean) {
        if (hasScannedImage) {
            try {
                val cacheFile = File(cacheDir, "scanned_image.jpg")
                if (cacheFile.exists()) {
                    binding.ivObject.setImageBitmap(BitmapFactory.decodeFile(cacheFile.absolutePath))
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

        binding.btnFacts.setBackgroundColor(modeColor)
        binding.rvFacts.layoutManager = LinearLayoutManager(this)
        binding.rvFacts.adapter = FactsAdapter(obj.facts)

        binding.btnFacts.setOnClickListener {
            showFacts = !showFacts
            binding.rvFacts.visibility = if (showFacts) View.VISIBLE else View.GONE
            binding.btnFacts.text = if (showFacts) "Hide Facts" else "Show Interesting Facts"
        }

        binding.btnStartQuiz.setBackgroundColor(modeColor)
        binding.btnStartQuiz.setOnClickListener {
            val intent = android.content.Intent(this, QuizActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_OBJECT_ID, obj.id)
            intent.putExtra(AppConstants.EXTRA_MODE, mode)
            startActivity(intent)
        }
    }

    private fun loadFromUrl(url: String) {
        Glide.with(this).load(url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.ivObject)
    }
}