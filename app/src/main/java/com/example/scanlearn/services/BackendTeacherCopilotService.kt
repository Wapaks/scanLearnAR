package com.example.scanlearn.services

class BackendTeacherCopilotService : TeacherCopilotService {

    override suspend fun generateLessonDraft(
        gradeLevel: String,
        quarterTitle: String,
        unitTitle: String,
        lessonTitleHint: String,
        lessonObjectiveHint: String
    ): TeacherCopilotService.LessonDraftSuggestion {
        throw UnsupportedOperationException("Gemini backend gateway is not connected yet.")
    }

    override suspend fun simplifyLesson(
        gradeLevel: String,
        title: String,
        objective: String,
        summary: String
    ): TeacherCopilotService.LessonDraftSuggestion {
        throw UnsupportedOperationException("Gemini backend gateway is not connected yet.")
    }

    override suspend fun generateQuizActivities(
        gradeLevel: String,
        lessonTitle: String,
        objective: String,
        summary: String
    ): List<TeacherCopilotService.ActivitySuggestion> {
        throw UnsupportedOperationException("Gemini backend gateway is not connected yet.")
    }

    override suspend fun generateMissionDraft(
        gradeLevel: String,
        sectionNames: List<String>,
        category: String,
        objectNames: List<String>
    ): TeacherCopilotService.MissionDraftSuggestion {
        throw UnsupportedOperationException("Gemini backend gateway is not connected yet.")
    }

    override suspend fun summarizeAnalytics(
        overview: String,
        lowConfidenceNotes: List<String>,
        weakQuizNotes: List<String>,
        categoryNotes: List<String>
    ): String {
        throw UnsupportedOperationException("Gemini backend gateway is not connected yet.")
    }
}
