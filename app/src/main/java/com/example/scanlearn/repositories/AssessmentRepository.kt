package com.example.scanlearn.repositories

import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.QuizAttempt
import com.example.scanlearn.services.RealtimeDbService

class AssessmentRepository(
    private val dbService: RealtimeDbService = RealtimeDbService()
) {

    fun getQuizAttempts(studentId: String, onResult: (List<QuizAttempt>) -> Unit) {
        dbService.getQuizAttempts(studentId, onResult)
    }

    fun saveQuizAttempt(
        studentId: String,
        attempt: QuizAttempt,
        onComplete: (String?) -> Unit = {}
    ) {
        dbService.saveQuizAttempt(studentId, attempt, onComplete)
    }

    fun buildLessonAssessmentAttempt(
        studentId: String,
        lesson: Lesson,
        score: Int,
        totalQuestions: Int,
        answers: List<Int>,
        mode: String,
        missionId: String = "",
        objectId: String = "",
        objectName: String = ""
    ): QuizAttempt {
        return QuizAttempt(
            studentId = studentId,
            lessonId = lesson.id,
            objectId = objectId,
            objectName = objectName,
            missionId = missionId,
            mode = mode,
            score = score,
            totalQuestions = totalQuestions,
            answers = answers
        )
    }
}
