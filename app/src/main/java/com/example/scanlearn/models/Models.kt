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
    val quiz: List<QuizQuestion> = emptyList()
) : Serializable

data class Mission(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val objectsToFind: List<String> = emptyList(),
    var completed: Boolean = false
) : Serializable

data class Submission(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val objectName: String = "",
    val learnings: String = "",
    val timestamp: String = "",
    val quizScore: Int = 0,
    val totalQuestions: Int = 0,
    val mode: String = ""
) : Serializable

data class ScannedObject(
    val objectId: String = "",
    val timestamp: String = "",
    val mode: String = ""
) : Serializable

data class StudentProgress(
    val userId: String = "",
    val name: String = "",
    val studentNumber: String = "",
    val section: String = "",
    val scannedCount: Int = 0,
    val submissionsCount: Int = 0
) : Serializable