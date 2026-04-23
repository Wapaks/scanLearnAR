package com.example.scanlearn.utils

import com.example.scanlearn.models.SectionRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SchoolStructure {

    private val gradeSectionMap = linkedMapOf(
        "Grade 1" to listOf("Kindness", "Wisdom", "Diligence", "Honesty", "Loyalty"),
        "Grade 2" to listOf("Sampaguita", "Rose", "Orchid", "Molave", "Camia"),
        "Grade 3" to listOf("Manga", "Melon", "Guyabano", "Kasoy", "Santol"),
        "Grade 4" to listOf("Narra", "Acacia", "Molave", "Yakal", "Mahogany"),
        "Grade 5" to listOf("Rizal", "Tandang Sora", "Lopez Jaena", "Andres Bonifacio", "Burgos"),
        "Grade 6" to listOf("Garnet", "Pearl", "Diamond", "Emerald", "Ruby")
    )

    val gradeLevels: List<String>
        get() = gradeSectionMap.keys.toList()

    fun sectionsForGrade(gradeLevel: String): List<String> {
        val normalizedGrade = normalizeGradeLevel(gradeLevel)
        return gradeSectionMap[normalizedGrade].orEmpty()
    }

    fun normalizeGradeLevel(gradeLevel: String): String {
        val cleaned = gradeLevel.trim()
        return gradeSectionMap.keys.firstOrNull { it.equals(cleaned, ignoreCase = true) }.orEmpty()
    }

    fun normalizeSectionName(section: String): String {
        val cleaned = section.trim()
        gradeSectionMap.values.flatten().forEach { knownSection ->
            if (knownSection.equals(cleaned, ignoreCase = true)) {
                return knownSection
            }
        }
        return cleaned
    }

    fun defaultGradeLevelForRole(role: String): String {
        return if (role.equals("teacher", ignoreCase = true)) {
            gradeLevels.first()
        } else {
            "Grade 3"
        }
    }

    fun resolveGradeLevel(gradeLevel: String, role: String = "student"): String {
        return normalizeGradeLevel(gradeLevel).ifBlank { defaultGradeLevelForRole(role) }
    }

    fun resolveSectionName(section: String): String {
        return normalizeSectionName(section)
    }

    fun buildSectionRecords(): List<SectionRecord> {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        return gradeSectionMap.flatMap { (gradeLevel, sections) ->
            sections.map { sectionName ->
                SectionRecord(
                    id = buildSectionId(gradeLevel, sectionName),
                    name = sectionName,
                    gradeLevel = gradeLevel,
                    createdAt = timestamp,
                    updatedAt = timestamp
                )
            }
        }
    }

    fun buildSectionId(gradeLevel: String, sectionName: String): String {
        return (gradeLevel + "_" + sectionName)
            .lowercase()
            .replace(" ", "_")
    }
}
