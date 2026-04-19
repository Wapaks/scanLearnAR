package com.example.scanlearn.repositories

import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.LessonActivity
import com.example.scanlearn.services.RealtimeDbService

class LessonRepository(
    private val dbService: RealtimeDbService = RealtimeDbService()
) {

    fun getLesson(lessonId: String, onResult: (Lesson?) -> Unit) {
        dbService.getLesson(lessonId, onResult)
    }

    fun getAllLessons(onResult: (List<Lesson>) -> Unit) {
        dbService.getAllLessons(onResult)
    }

    fun getLessonsForUnit(unitId: String, onResult: (List<Lesson>) -> Unit) {
        dbService.getLessonsForUnit(unitId, onResult)
    }

    fun getActivitiesForLesson(lessonId: String, onResult: (List<LessonActivity>) -> Unit) {
        dbService.getActivitiesForLesson(lessonId, onResult)
    }

    fun getLinkedObjects(lesson: Lesson, onResult: (List<LearningObject>) -> Unit) {
        dbService.getLearningObjects { objects ->
            val linked = objects.filter { it.id in lesson.linkedObjectIds }
            onResult(linked)
        }
    }

    fun saveLesson(lesson: Lesson, onComplete: (Boolean) -> Unit = {}) {
        dbService.saveLesson(lesson, onComplete)
    }

    fun saveLessonActivity(activity: LessonActivity, onComplete: (Boolean) -> Unit = {}) {
        dbService.saveLessonActivity(activity, onComplete)
    }

    fun replaceActivitiesForLesson(
        lessonId: String,
        activities: List<LessonActivity>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        dbService.deleteActivitiesForLesson(lessonId) { deleted ->
            if (!deleted) {
                onComplete(false)
                return@deleteActivitiesForLesson
            }
            if (activities.isEmpty()) {
                onComplete(true)
                return@deleteActivitiesForLesson
            }

            var remaining = activities.size
            var allSuccessful = true
            activities.forEach { activity ->
                dbService.saveLessonActivity(activity) { saved ->
                    if (!saved) allSuccessful = false
                    remaining -= 1
                    if (remaining == 0) onComplete(allSuccessful)
                }
            }
        }
    }
}
