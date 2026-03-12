package com.example.scanlearn.models

import java.io.Serializable

data class User(
    val id: String,
    val name: String,
    val email: String,
    val studentNumber: String
) : Serializable

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
) : Serializable

data class LearningObject(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val imageUrl: String,
    val facts: List<String>,
    val quiz: List<QuizQuestion>
) : Serializable

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val objectsToFind: List<String>,
    var completed: Boolean = false
) : Serializable

data class Submission(
    val id: String,
    val studentId: String,
    val studentName: String,
    val objectName: String,
    val learnings: String,
    val timestamp: String,
    val quizScore: Int,
    val totalQuestions: Int
) : Serializable

data class ScannedObject(
    val objectId: String,
    val timestamp: String,
    val mode: String
) : Serializable
