package com.example.scanlearn.models

import java.io.Serializable

data class ObjectSelectionBox(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0
) : Serializable

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val studentNumber: String = "",
    val role: String = "student",
    val section: String = "",
    val sectionId: String = "",
    val gradeLevel: String = "",
    val schoolId: String = "",
    val status: String = "active",
    val createdAt: String = ""
) : Serializable

data class ChatConversation(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantRoles: Map<String, String> = emptyMap(),
    val unreadCounts: Map<String, Int> = emptyMap(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    val lastUpdatedAt: String = "",
    val createdAt: String = ""
) : Serializable

data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val message: String = "",
    val createdAt: String = ""
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
    val imageUrls: List<String> = emptyList(),
    val facts: List<String> = emptyList(),
    val quiz: List<QuizQuestion> = emptyList(),
    val aliases: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val gradeLevels: List<String> = emptyList(),
    val linkedLessonIds: List<String> = emptyList(),
    val difficulty: String = "",
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
    val missionType: String = "",
    val gradeLevel: String = "",
    val quarterId: String = "",
    val lessonIds: List<String> = emptyList(),
    val objectsToFind: List<String> = emptyList(),
    val sectionIds: List<String> = emptyList(),
    val category: String = "",
    val passingScore: Int = 0,
    val requiredCount: Int = 0,
    val active: Boolean = true,
    val createdBy: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val aiGenerated: Boolean = false,
    val aiSource: String = "",
    val aiPromptVersion: String = "",
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
    val lessonId: String = "",
    val objectId: String = "",
    val objectName: String = "",
    val missionId: String = "",
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
    val lessonId: String = "",
    val missionId: String = "",
    val categoryContext: String = "",
    val predictionSource: String = "mlkit",
    val suggestions: List<String> = emptyList(),
    val selectedObjectId: String = "",
    val confidence: Float = 0f,
    val manualCorrection: Boolean = false,
    val selectedBox: ObjectSelectionBox = ObjectSelectionBox(),
    val selectionApplied: Boolean = false,
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

data class CurriculumMap(
    val gradeLevel: String = "",
    val title: String = "",
    val quarterIds: List<String> = emptyList()
) : Serializable

data class Quarter(
    val id: String = "",
    val gradeLevel: String = "",
    val quarterNumber: Int = 0,
    val title: String = "",
    val description: String = "",
    val unitIds: List<String> = emptyList(),
    val orderIndex: Int = 0,
    val status: String = "draft"
) : Serializable

data class Unit(
    val id: String = "",
    val gradeLevel: String = "",
    val quarterId: String = "",
    val title: String = "",
    val overview: String = "",
    val lessonIds: List<String> = emptyList(),
    val orderIndex: Int = 0,
    val estimatedMinutes: Int = 0,
    val status: String = "draft"
) : Serializable

data class Lesson(
    val id: String = "",
    val gradeLevel: String = "",
    val quarterId: String = "",
    val unitId: String = "",
    val title: String = "",
    val objective: String = "",
    val summary: String = "",
    val lessonType: String = "",
    val scanSupported: Boolean = false,
    val competencyIds: List<String> = emptyList(),
    val activityIds: List<String> = emptyList(),
    val linkedObjectIds: List<String> = emptyList(),
    val assessmentId: String = "",
    val orderIndex: Int = 0,
    val estimatedMinutes: Int = 0,
    val difficulty: String = "",
    val status: String = "draft",
    val createdBy: String = "",
    val aiGenerated: Boolean = false,
    val aiSource: String = "",
    val aiPromptVersion: String = "",
    val updatedAt: String = ""
) : Serializable

data class LessonActivity(
    val id: String = "",
    val lessonId: String = "",
    val type: String = "",
    val prompt: String = "",
    val instructions: String = "",
    val content: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val explanation: String = "",
    val points: Int = 0,
    val orderIndex: Int = 0
) : Serializable

data class Competency(
    val id: String = "",
    val gradeLevel: String = "",
    val quarterId: String = "",
    val code: String = "",
    val statement: String = "",
    val masteryThreshold: Int = 75
) : Serializable

data class StudentLessonProgress(
    val lessonId: String = "",
    val status: String = "not_started",
    val attempts: Int = 0,
    val bestScore: Int = 0,
    val lastScore: Int = 0,
    val masteryStatus: String = "not_started",
    val completedActivities: List<String> = emptyList(),
    val completedAt: String = "",
    val lastOpenedAt: String = ""
) : Serializable

data class MasteryRecord(
    val competencyId: String = "",
    val gradeLevel: String = "",
    val quarterId: String = "",
    val evidenceIds: List<String> = emptyList(),
    val masteryPercent: Int = 0,
    val masteryStatus: String = "not_started",
    val updatedAt: String = ""
) : Serializable

data class AiDraftVariant(
    val id: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val feature: String = "",
    val promptVersion: String = "",
    val modelName: String = "",
    val generatedText: String = "",
    val status: String = "generated",
    val createdBy: String = "",
    val createdAt: String = ""
) : Serializable

data class AiUsageLog(
    val id: String = "",
    val userId: String = "",
    val role: String = "",
    val feature: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val modelName: String = "",
    val promptVersion: String = "",
    val status: String = "",
    val errorMessage: String = "",
    val createdAt: String = ""
) : Serializable
