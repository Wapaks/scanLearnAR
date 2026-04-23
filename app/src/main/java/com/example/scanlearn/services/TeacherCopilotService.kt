package com.example.scanlearn.services

interface TeacherCopilotService {

    suspend fun generateLessonDraft(
        gradeLevel: String,
        quarterTitle: String,
        unitTitle: String,
        lessonTitleHint: String,
        lessonObjectiveHint: String
    ): LessonDraftSuggestion

    suspend fun simplifyLesson(
        gradeLevel: String,
        title: String,
        objective: String,
        summary: String
    ): LessonDraftSuggestion

    suspend fun generateQuizActivities(
        gradeLevel: String,
        lessonTitle: String,
        objective: String,
        summary: String
    ): List<ActivitySuggestion>

    suspend fun generateMissionDraft(
        gradeLevel: String,
        sectionNames: List<String>,
        category: String,
        objectNames: List<String>
    ): MissionDraftSuggestion

    suspend fun summarizeAnalytics(
        overview: String,
        lowConfidenceNotes: List<String>,
        weakQuizNotes: List<String>,
        categoryNotes: List<String>
    ): String

    data class LessonDraftSuggestion(
        val title: String,
        val objective: String,
        val summary: String,
        val rawText: String
    )

    data class ActivitySuggestion(
        val type: String,
        val prompt: String,
        val instructions: String,
        val options: List<String>,
        val answer: String
    )

    data class MissionDraftSuggestion(
        val title: String,
        val description: String,
        val category: String,
        val rawText: String
    )
}

object TeacherCopilotDefaults {
    const val DEFAULT_MODEL = "gemini-2.5-flash"
    const val PROMPT_VERSION = "teacher-copilot-v1"
    const val PROVIDER_FIREBASE = "firebase_ai"
    const val PROVIDER_GEMINI_BACKEND = "gemini_backend"
}
