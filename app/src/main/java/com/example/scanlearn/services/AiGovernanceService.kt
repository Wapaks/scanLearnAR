package com.example.scanlearn.services

import com.example.scanlearn.models.AiDraftVariant
import com.example.scanlearn.models.AiUsageLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AiGovernanceService(
    private val dbService: RealtimeDbService = RealtimeDbService()
) {

    fun saveDraftVariant(
        targetType: String,
        targetId: String,
        feature: String,
        generatedText: String,
        createdBy: String,
        modelName: String,
        promptVersion: String,
        status: String = "generated",
        onComplete: (Boolean) -> Unit = {}
    ) {
        dbService.saveAiDraftVariant(
            AiDraftVariant(
                targetType = targetType,
                targetId = targetId,
                feature = feature,
                generatedText = generatedText,
                createdBy = createdBy,
                modelName = modelName,
                promptVersion = promptVersion,
                status = status,
                createdAt = nowIsoString()
            ),
            onComplete
        )
    }

    fun logUsage(
        userId: String,
        role: String,
        feature: String,
        targetType: String,
        targetId: String,
        modelName: String,
        promptVersion: String,
        status: String,
        errorMessage: String = "",
        onComplete: (Boolean) -> Unit = {}
    ) {
        dbService.saveAiUsageLog(
            AiUsageLog(
                userId = userId,
                role = role,
                feature = feature,
                targetType = targetType,
                targetId = targetId,
                modelName = modelName,
                promptVersion = promptVersion,
                status = status,
                errorMessage = errorMessage,
                createdAt = nowIsoString()
            ),
            onComplete
        )
    }

    private fun nowIsoString(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
    }
}
