package com.example.scanlearn.services

import com.example.scanlearn.models.LearningData

class DatabaseSeeder(private val dbService: RealtimeDbService) {

    fun seed(onComplete: () -> Unit = {}) {
        dbService.seedLearningObjects(LearningData.LEARNING_OBJECTS) {
            onComplete()
        }
    }
}