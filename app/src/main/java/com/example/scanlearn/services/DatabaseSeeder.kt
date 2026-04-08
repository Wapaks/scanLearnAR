package com.example.scanlearn.services

import com.example.scanlearn.models.LearningData
import com.example.scanlearn.models.LearningObject

class DatabaseSeeder(private val dbService: RealtimeDbService) {

    fun seed(onComplete: () -> Unit = {}) {
        val allObjects = buildAllObjects()
        dbService.seedLearningObjects(allObjects) {
            onComplete()
        }
    }

    private fun buildAllObjects(): List<LearningObject> {
        val specific = LearningData.SPECIFIC_TEMPLATES.values.toList()
        val general = LearningData.RANDOM_POOL_BY_CATEGORY.values.flatten()
        return specific + general
    }
}