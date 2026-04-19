package com.example.scanlearn.utils

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

class ObjectDetectorHelper {

    fun detectObjects(
        bitmap: Bitmap,
        onResult: (List<Rect>) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .build()
        val detector = ObjectDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { objects ->
                onResult(
                    objects
                        .map(DetectedObject::getBoundingBox)
                        .filter { box -> box.width() > 24 && box.height() > 24 }
                )
                detector.close()
            }
            .addOnFailureListener {
                onResult(emptyList())
                detector.close()
            }
    }
}
