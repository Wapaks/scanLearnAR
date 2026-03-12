package com.example.scanlearn.utils

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class ObjectClassifier {

    private val labelToObjectId = mapOf(
        // Apple
        "apple" to "apple",
        "fruit" to "apple",
        "food" to "apple",
        "granny smith" to "apple",

        // Leaf
        "leaf" to "leaf",
        "plant" to "leaf",
        "tree" to "leaf",
        "flower" to "leaf",
        "grass" to "leaf",
        "vegetation" to "leaf",
        "fern" to "leaf",
        "herb" to "leaf",
        "botany" to "leaf",

        // Rock
        "rock" to "rock",
        "stone" to "rock",
        "mineral" to "rock",
        "geology" to "rock",
        "boulder" to "rock",
        "gravel" to "rock",
        "pebble" to "rock",
        "cliff" to "rock",

        // Water
        "water" to "water",
        "lake" to "water",
        "ocean" to "water",
        "river" to "water",
        "sea" to "water",
        "liquid" to "water",
        "pond" to "water",
        "swimming pool" to "water",
        "rain" to "water",

        // Sun
        "sun" to "sun",
        "sky" to "sun",
        "sunlight" to "sun",
        "sunrise" to "sun",
        "sunset" to "sun",
        "solar" to "sun",
        "atmosphere" to "sun",
        "cloud" to "sun"
    )

    fun classify(bitmap: Bitmap, onResult: (String?) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.6f)  // 60% confidence minimum
            .build()

        val labeler = ImageLabeling.getClient(options)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                // Log all detected labels for debugging
                for (label in labels) {
                    android.util.Log.d("MLKit", "Detected: ${label.text} (${label.confidence})")
                }

                // Try to match any detected label to our objects
                var matchedObjectId: String? = null
                outer@ for (label in labels) {
                    val labelText = label.text.lowercase()
                    for ((keyword, objectId) in labelToObjectId) {
                        if (labelText.contains(keyword) || keyword.contains(labelText)) {
                            matchedObjectId = objectId
                            break@outer
                        }
                    }
                }

                onResult(matchedObjectId)
                labeler.close()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("MLKit", "Labeling failed: ${e.message}")
                onResult(null)
                labeler.close()
            }
    }
}