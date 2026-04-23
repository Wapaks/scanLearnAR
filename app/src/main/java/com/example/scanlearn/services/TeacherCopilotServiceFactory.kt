package com.example.scanlearn.services

object TeacherCopilotServiceFactory {

    fun create(): TeacherCopilotService {
        // Keep the existing Firebase/Gemini path active until the backend gateway is ready.
        return GeminiTeacherCopilotService()
    }
}
