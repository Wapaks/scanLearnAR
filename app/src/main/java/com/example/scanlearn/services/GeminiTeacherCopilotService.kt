package com.example.scanlearn.services

import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig

class GeminiTeacherCopilotService : TeacherCopilotService {

    private val teacherModel by lazy {
        FirebaseAI.instance.generativeModel(
            modelName = TeacherCopilotDefaults.DEFAULT_MODEL,
            generationConfig = generationConfig {
                temperature = 0.4f
                maxOutputTokens = 1200
            },
            systemInstruction = content("system") {
                text(
                    "You are the ScanLearn Teacher Copilot. " +
                        "You write safe, age-appropriate, curriculum-first elementary content for the teacher's selected grade level. " +
                        "Keep responses short, clear, and useful for elementary learners. " +
                        "Do not produce unsafe, off-topic, or unbounded content. " +
                        "When asked for structured output, strictly follow the requested labels."
                )
            }
        )
    }

    override suspend fun generateLessonDraft(
        gradeLevel: String,
        quarterTitle: String,
        unitTitle: String,
        lessonTitleHint: String,
        lessonObjectiveHint: String
    ): TeacherCopilotService.LessonDraftSuggestion {
        val prompt = """
            Create a short curriculum-aligned lesson draft for ScanLearn.
            Grade: $gradeLevel
            Quarter: $quarterTitle
            Unit: $unitTitle
            Lesson title hint: $lessonTitleHint
            Objective hint: $lessonObjectiveHint

            Return exactly this format:
            TITLE: <short lesson title>
            OBJECTIVE: <one-sentence objective>
            SUMMARY: <2 to 4 short sentences for the selected grade level>
        """.trimIndent()

        return parseLessonDraft(runTextPrompt(prompt))
    }

    override suspend fun simplifyLesson(
        gradeLevel: String,
        title: String,
        objective: String,
        summary: String
    ): TeacherCopilotService.LessonDraftSuggestion {
        val prompt = """
            Simplify this lesson for $gradeLevel learners.
            Title: $title
            Objective: $objective
            Summary: $summary

            Return exactly this format:
            TITLE: <simplified title>
            OBJECTIVE: <simplified objective>
            SUMMARY: <simplified 2 to 4 short sentences>
        """.trimIndent()

        return parseLessonDraft(runTextPrompt(prompt))
    }

    override suspend fun generateQuizActivities(
        gradeLevel: String,
        lessonTitle: String,
        objective: String,
        summary: String
    ): List<TeacherCopilotService.ActivitySuggestion> {
        val prompt = """
            Create 3 short formative activities for ScanLearn.
            Grade: $gradeLevel
            Lesson: $lessonTitle
            Objective: $objective
            Summary: $summary

            Rules:
            - Make 2 multiple-choice activities and 1 short-answer activity.
            - Keep each prompt short and age-appropriate.
            - Use simple English.
            - The correct answer must be clear.

            Return exactly this pattern:
            ACTIVITY 1
            TYPE: <multiple_choice or short_answer>
            PROMPT: <prompt>
            INSTRUCTIONS: <short instruction>
            OPTION_1: <text or blank>
            OPTION_2: <text or blank>
            OPTION_3: <text or blank>
            OPTION_4: <text or blank>
            ANSWER: <correct answer text>

            ACTIVITY 2
            ...

            ACTIVITY 3
            ...
        """.trimIndent()

        return parseActivities(runTextPrompt(prompt))
    }

    override suspend fun generateMissionDraft(
        gradeLevel: String,
        sectionNames: List<String>,
        category: String,
        objectNames: List<String>
    ): TeacherCopilotService.MissionDraftSuggestion {
        val prompt = """
            Create a teacher mission draft for ScanLearn.
            Grade: $gradeLevel
            Sections: ${sectionNames.joinToString()}
            Category: $category
            Selected objects: ${objectNames.joinToString()}

            Return exactly this format:
            TITLE: <short mission title>
            DESCRIPTION: <2 to 3 short sentences>
            CATEGORY: <single lowercase category word>
        """.trimIndent()

        return parseMissionDraft(runTextPrompt(prompt))
    }

    override suspend fun summarizeAnalytics(
        overview: String,
        lowConfidenceNotes: List<String>,
        weakQuizNotes: List<String>,
        categoryNotes: List<String>
    ): String {
        val prompt = """
            Summarize these ScanLearn analytics for a teacher.
            Overview: $overview
            Low-confidence scan trends: ${lowConfidenceNotes.joinToString(" | ")}
            Weak quiz trends: ${weakQuizNotes.joinToString(" | ")}
            Category trends: ${categoryNotes.joinToString(" | ")}

            Write:
            - one short overview sentence
            - one sentence on the biggest concern
            - one sentence suggesting a practical next classroom action
        """.trimIndent()

        return runTextPrompt(prompt)
    }

    private suspend fun runTextPrompt(prompt: String): String {
        val response = teacherModel.generateContent(prompt)
        return response.text?.trim().orEmpty().ifBlank {
            throw IllegalStateException("Gemini returned an empty response.")
        }
    }

    private fun parseLessonDraft(text: String): TeacherCopilotService.LessonDraftSuggestion {
        return TeacherCopilotService.LessonDraftSuggestion(
            title = extractValue(text, "TITLE"),
            objective = extractValue(text, "OBJECTIVE"),
            summary = extractValue(text, "SUMMARY"),
            rawText = text
        )
    }

    private fun parseMissionDraft(text: String): TeacherCopilotService.MissionDraftSuggestion {
        return TeacherCopilotService.MissionDraftSuggestion(
            title = extractValue(text, "TITLE"),
            description = extractValue(text, "DESCRIPTION"),
            category = extractValue(text, "CATEGORY").lowercase(),
            rawText = text
        )
    }

    private fun parseActivities(text: String): List<TeacherCopilotService.ActivitySuggestion> {
        val chunks = text.split(Regex("ACTIVITY\\s+\\d+")).map { it.trim() }.filter { it.isNotBlank() }
        return chunks.mapNotNull { chunk ->
            val type = extractValue(chunk, "TYPE")
            val prompt = extractValue(chunk, "PROMPT")
            val instructions = extractValue(chunk, "INSTRUCTIONS")
            val answer = extractValue(chunk, "ANSWER")
            if (type.isBlank() || prompt.isBlank() || answer.isBlank()) {
                null
            } else {
                TeacherCopilotService.ActivitySuggestion(
                    type = type,
                    prompt = prompt,
                    instructions = instructions,
                    options = listOf(
                        extractValue(chunk, "OPTION_1"),
                        extractValue(chunk, "OPTION_2"),
                        extractValue(chunk, "OPTION_3"),
                        extractValue(chunk, "OPTION_4")
                    ).filter { it.isNotBlank() },
                    answer = answer
                )
            }
        }
    }

    private fun extractValue(text: String, label: String): String {
        val pattern = Regex("$label:\\s*(.+)", RegexOption.IGNORE_CASE)
        return pattern.find(text)?.groupValues?.getOrNull(1)?.trim().orEmpty()
    }

}
