package com.example.scanlearn.models

import java.io.Serializable

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val studentNumber: String = "",
    val role: String = "student",
    val section: String = ""
) : Serializable

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: Int = 0
) : Serializable

data class LearningObject(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val facts: List<String> = emptyList(),
    val quiz: List<QuizQuestion> = emptyList(),
    val status: String = "published",
    val createdBy: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
) : Serializable

data class DetectionLabel(
    val text: String = "",
    val confidence: Float = 0f
) : Serializable

data class ClassificationResult(
    val category: String? = null,
    val specificId: String? = null,
    val labels: List<DetectionLabel> = emptyList()
) : Serializable

data class Mission(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val objectsToFind: List<String> = emptyList(),
    val sectionIds: List<String> = emptyList(),
    val category: String = "",
    val active: Boolean = true,
    val createdBy: String = "",
    val createdAt: String = "",
    val recommendedForStudentId: String = "",
    val progressPercent: Int = 0,
    val completedObjectIds: List<String> = emptyList(),
    var completed: Boolean = false
) : Serializable

data class StudentMissionProgress(
    val missionId: String = "",
    val completedObjectIds: List<String> = emptyList(),
    val progressPercent: Int = 0,
    val completed: Boolean = false,
    val updatedAt: String = ""
) : Serializable

data class QuizAttempt(
    val id: String = "",
    val studentId: String = "",
    val objectId: String = "",
    val objectName: String = "",
    val mode: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val answers: List<Int> = emptyList(),
    val scanAttemptId: String = "",
    val completedAt: String = ""
) : Serializable

data class Submission(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val objectId: String = "",
    val objectName: String = "",
    val learnings: String = "",
    val timestamp: String = "",
    val quizScore: Int = 0,
    val totalQuestions: Int = 0,
    val mode: String = "",
    val quizAttemptId: String = "",
    val scanAttemptId: String = "",
    val scanConfidence: Float = 0f,
    val manualCorrection: Boolean = false
) : Serializable

data class ScannedObject(
    val objectId: String = "",
    val timestamp: String = "",
    val mode: String = ""
) : Serializable

data class ScanAttempt(
    val id: String = "",
    val studentId: String = "",
    val mode: String = "",
    val categoryContext: String = "",
    val predictionSource: String = "mlkit",
    val suggestions: List<String> = emptyList(),
    val selectedObjectId: String = "",
    val confidence: Float = 0f,
    val manualCorrection: Boolean = false,
    val status: String = "confirmed",
    val createdAt: String = ""
) : Serializable

data class StudentProgress(
    val userId: String = "",
    val name: String = "",
    val studentNumber: String = "",
    val section: String = "",
    val scannedCount: Int = 0,
    val submissionsCount: Int = 0,
    val quizAttemptsCount: Int = 0,
    val averageScorePercent: Int = 0,
    val manualCorrectionsCount: Int = 0,
    val lowConfidenceCount: Int = 0
) : Serializable

data class WeakTopicInsight(
    val itemId: String = "",
    val objectName: String = "",
    val averageScorePercent: Int = 0,
    val attemptsCount: Int = 0
) : Serializable

data class LowConfidenceScanInsight(
    val objectName: String = "",
    val category: String = "",
    val confidencePercent: Int = 0,
    val manualCorrection: Boolean = false,
    val createdAt: String = ""
) : Serializable

data class LearningObjectAnalytics(
    val objectId: String = "",
    val objectName: String = "",
    val category: String = "",
    val status: String = "published",
    val totalScanSelections: Int = 0,
    val lowConfidenceSelections: Int = 0,
    val manualCorrections: Int = 0,
    val quizAttempts: Int = 0,
    val averageQuizScorePercent: Int = 0,
    val recentLearners: Int = 0
) : Serializable

data class CategoryAnalytics(
    val category: String = "",
    val totalSelections: Int = 0,
    val manualCorrections: Int = 0,
    val lowConfidenceSelections: Int = 0,
    val averageQuizScorePercent: Int = 0
) : Serializable
