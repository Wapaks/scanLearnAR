package com.example.scanlearn.services

import com.example.scanlearn.models.Grade3PilotCurriculum
import com.example.scanlearn.models.LearningData
import com.example.scanlearn.models.LearningObject

class DatabaseSeeder(private val dbService: RealtimeDbService) {

    fun seed(onComplete: () -> Unit = {}) {
        val allObjects = buildAllObjects()
        dbService.seedLearningObjects(allObjects) {
            onComplete()
        }
    }

    fun ensureCoreDataSeeded(onComplete: () -> Unit = {}) {
        dbService.getLearningObjects { existingObjects ->
            val seedObjects = {
                if (existingObjects.isEmpty()) {
                    seed {
                        ensureGrade3PilotCurriculumSeeded(onComplete)
                    }
                } else {
                    ensureGrade3PilotCurriculumSeeded(onComplete)
                }
            }
            seedObjects()
        }
    }

    fun ensureGrade3PilotCurriculumSeeded(onComplete: () -> Unit = {}) {
        dbService.getCurriculumMap(Grade3PilotCurriculum.GRADE_LEVEL) { existingMap ->
            if (existingMap != null) {
                onComplete()
                return@getCurriculumMap
            }

            dbService.seedCurriculumContent(
                curriculumMaps = Grade3PilotCurriculum.curriculumMaps,
                quarters = Grade3PilotCurriculum.quarters,
                units = Grade3PilotCurriculum.units,
                lessons = Grade3PilotCurriculum.lessons,
                activities = Grade3PilotCurriculum.activities,
                competencies = Grade3PilotCurriculum.competencies
            ) {
                onComplete()
            }
        }
    }

    private fun buildAllObjects(): List<LearningObject> {
        val specific = LearningData.SPECIFIC_TEMPLATES.values.toList()
        val general = LearningData.RANDOM_POOL_BY_CATEGORY.values.flatten()
        return specific + general
    }
}
